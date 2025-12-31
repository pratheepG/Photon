package com.photon.alerts.repository;

import com.photon.alerts.entity.SubscriptionTopic;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SubscriptionTopicRepository extends CrudRepository<SubscriptionTopic, UUID> { }