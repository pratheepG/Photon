package com.photon.content.location.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.photon.content.location.dto.response.DistrictResponse;
import com.photon.content.location.dto.response.StateResponse;
import com.photon.content.location.dto.response.CityResponse;
import com.photon.content.location.entity.District;
import com.photon.content.location.entity.State;
import com.photon.content.location.entity.City;
import com.photon.content.location.repository.DistrictRepository;
import com.photon.content.location.repository.StateRepository;
import com.photon.dto.ApiResponseDto;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class LocalityService {

    private static final int MAX_PAGE_SIZE = 200;
    private static final int DEFAULT_PAGE_SIZE = 50;

    private final R2dbcEntityTemplate template;
    private final StateRepository stateRepo;
    private final DistrictRepository districtRepo;
    private final Cache<String, List<StateResponse>> stateListCache;
    private final Cache<String, StateResponse> stateByCodeCache;

    public LocalityService(R2dbcEntityTemplate template, StateRepository stateRepo,
                           DistrictRepository districtRepo, Cache<String, List<StateResponse>> stateListCache,
                           Cache<String, StateResponse> stateByCodeCache) {
        this.template = template;
        this.stateRepo = stateRepo;
        this.districtRepo = districtRepo;
        this.stateListCache = stateListCache;
        this.stateByCodeCache = stateByCodeCache;
    }

    private int normalizeSize(int size) {
        if (size <= 0) return DEFAULT_PAGE_SIZE;
        return Math.min(size, MAX_PAGE_SIZE);
    }

    public Mono<ApiResponseDto<List<StateResponse>>> searchStates(String q, int page, int size) {
        int normalized = normalizeSize(size);
        int offset = Math.max(0, page) * normalized;

        if ((q == null || q.isBlank()) && page == 0) {
            List<StateResponse> cached = stateListCache.getIfPresent("FULL_LIST");
            if (cached != null) {
                int totalPages = 1;
                return Mono.just(SuccessEnum.SUCCESS.getSuccessResponseBody(cached, "States (cached)", 0, normalized, totalPages, cached.size()));
            }
        }

        Query qObj = Query.empty();
        if (q != null && !q.isBlank()) {
            qObj = Query.query(Criteria.where("name").like("%" + q + "%").ignoreCase(true));
        }

        Mono<List<StateResponse>> listMono = template.select(qObj.sort(Sort.by(Sort.Direction.ASC, "name")).limit(normalized).offset(offset), State.class)
                .map(s -> new StateResponse(s.getId(), s.getName(), s.getCode()))
                .collectList();

        Mono<Long> countMono = (q == null || q.isBlank()) ?
                stateRepo.count() :
                template.count(Query.query(Criteria.where("name").like("%" + q + "%").ignoreCase(true)), State.class);

        return Mono.zip(listMono, countMono)
                .flatMap(tuple -> {
                    List<StateResponse> list = tuple.getT1();
                    long total = tuple.getT2();
                    int totalPages = (int) Math.ceil((double) total / normalized);

                    if ((q == null || q.isBlank()) && page == 0) {
                        stateListCache.put("FULL_LIST", list);
                    }
                    return Mono.just(SuccessEnum.SUCCESS.getSuccessResponseBody(list, "States fetched").page(page).size(normalized).totalPages(totalPages).totalRecords((int) total));
                })
                .onErrorResume(ex -> Mono.error(new ApplicationException(ExceptionEnum.ERR_1012.getErrorResponseBody("Error fetching states"), HttpStatus.INTERNAL_SERVER_ERROR)));
    }

    public Mono<ApiResponseDto<StateResponse>> getStateByCode(String stateCode) {
        if (stateCode == null || stateCode.isBlank()) {
            return Mono.error(new ApplicationException(ExceptionEnum.ERR_1002.getErrorResponseBody("stateCode required"), HttpStatus.BAD_REQUEST));
        }
        String key = stateCode.toUpperCase();

        StateResponse cached = stateByCodeCache.getIfPresent(key);
        if (cached != null) {
            return Mono.just(SuccessEnum.SUCCESS.getSuccessResponseBody(cached, "State (cached)"));
        }

        return stateRepo.findByCodeIgnoreCase(key)
                .map(s -> new StateResponse(s.getId(), s.getName(), s.getCode()))
                .flatMap(sr -> {
                    stateByCodeCache.put(key, sr);
                    return Mono.just(SuccessEnum.SUCCESS.getSuccessResponseBody(sr, "State found"));
                })
                .switchIfEmpty(Mono.error(new ApplicationException(ExceptionEnum.ERR_1004.getErrorResponseBody("State not found"), HttpStatus.NOT_FOUND)))
                .onErrorResume(ex -> Mono.error(new ApplicationException(ExceptionEnum.ERR_1012.getErrorResponseBody("Error fetching state"), HttpStatus.INTERNAL_SERVER_ERROR)));
    }

    public Mono<ApiResponseDto<List<DistrictResponse>>> searchDistricts(String name, Long stateId, int page, int size) {
        int normalized = normalizeSize(size);
        long offset = (long) Math.max(0, page) * normalized;

        Mono<Flux<District>> baseFluxMono;
        if (stateId != null) {
            baseFluxMono = stateRepo.findById(stateId)
                    .flatMapMany(s -> districtRepo.findByStateId(s.getId()))
                    .collectList()
                    .map(Flux::fromIterable);
        } else {
            if (name == null || name.isBlank()) {
                baseFluxMono = Mono.just(districtRepo.findAllByOrderByNameAsc());
            } else {
                baseFluxMono = Mono.just(districtRepo.findByNameContainingIgnoreCase(name));
            }
        }

        Flux<DistrictResponse> paged = baseFluxMono.flatMapMany(f -> f)
                .skip(offset)
                .take(normalized)
                .map(d -> new DistrictResponse(d.getId(), d.getName(), d.getStateId(), d.getStateCode()));

        Mono<Long> countMono;
        if (stateId != null ) {
            countMono = stateRepo.findById(stateId)
                    .flatMapMany(s -> districtRepo.findByStateId(s.getId()))
                    .count();
        } else {
            countMono = (name == null || name.isBlank()) ? districtRepo.count() : districtRepo.findByNameContainingIgnoreCase(name).count();
        }

        return Mono.zip(paged.collectList(), countMono)
                .map(tuple -> {
                    List<DistrictResponse> list = tuple.getT1();
                    long total = tuple.getT2();
                    int totalPages = (int) Math.ceil((double) total / normalized);
                    return SuccessEnum.SUCCESS.getSuccessResponseBody(list, "Districts fetched").page(page).size(normalized).totalPages(totalPages).totalRecords((int) total);
                })
                .onErrorResume(ex -> Mono.error(new ApplicationException(ExceptionEnum.ERR_1012.getErrorResponseBody("Error fetching districts"), HttpStatus.INTERNAL_SERVER_ERROR)));
    }

    /**
     * Search cities by name / districtId / pinCode. All filters are ANDed if provided.
     */
    public Mono<ApiResponseDto<List<CityResponse>>> searchCity(String cityName, Long districtId, String pinCode, int page, int size) {
        int normalized = normalizeSize(size);
        int offset = Math.max(0, page) * normalized;

        Criteria criteria = Criteria.empty();
        if (cityName != null && !cityName.isBlank()) {
            criteria = (criteria.isEmpty() ? Criteria.where("name").like("%" + cityName + "%").ignoreCase(true) :
                    criteria.and("name").like("%" + cityName + "%").ignoreCase(true));
        }
        if (districtId != null) {
            criteria = (criteria.isEmpty() ? Criteria.where("district_id").is(districtId) :
                    criteria.and("district_id").is(districtId));
        }
        if (pinCode != null && !pinCode.isBlank()) {
            criteria = (criteria.isEmpty() ? Criteria.where("pin_code").is(pinCode) :
                    criteria.and("pin_code").is(pinCode));
        }

        Query query = Query.query(criteria).sort(Sort.by(Sort.Direction.ASC, "name")).limit(normalized).offset(offset);

        Mono<List<CityResponse>> listMono = template.select(query, City.class)
                .map(c -> new CityResponse(c.getId(), c.getName(), c.getPinCode(), c.getDistrictId()))
                .collectList();

        Mono<Long> countMono = template.count(Query.query(criteria), City.class);

        return Mono.zip(listMono, countMono)
                .map(tuple -> {
                    List<CityResponse> list = tuple.getT1();
                    long total = tuple.getT2();
                    int totalPages = (int) Math.ceil((double) total / normalized);
                    return SuccessEnum.SUCCESS.getSuccessResponseBody(list, "City fetched").page(page).size(normalized).totalPages(totalPages).totalRecords((int) total);
                })
                .onErrorResume(ex -> Mono.error(new ApplicationException(ExceptionEnum.ERR_1012.getErrorResponseBody("Error fetching cities"), HttpStatus.INTERNAL_SERVER_ERROR)));
    }
}