-- Create subscriber_device_map Table
CREATE TABLE subscriber_device_map (
    subscriber_id UUID NOT NULL,
    device_info_id UUID NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,

    PRIMARY KEY (subscriber_id, device_info_id),
    FOREIGN KEY (subscriber_id) REFERENCES subscriber(id),
    FOREIGN KEY (device_info_id) REFERENCES device_info(id)
);

CREATE UNIQUE INDEX uniq_active_device_per_user
ON subscriber_device_map(subscriber_id)
WHERE is_active = TRUE;