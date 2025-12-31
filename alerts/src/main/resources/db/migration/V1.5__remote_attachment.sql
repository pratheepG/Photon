-- Create remote_attachment Table
CREATE TABLE remote_attachment (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGSERIAL NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    download_url TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);