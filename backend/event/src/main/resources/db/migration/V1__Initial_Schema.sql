CREATE TABLE app_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE user_roles (
    user_id BIGINT,
    role VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
);

CREATE TABLE app_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    image_url VARCHAR(255),
    event_time DATETIME NOT NULL,
    is_hidden BOOLEAN DEFAULT FALSE,
    owner_id BIGINT NOT NULL,
    capacity INTEGER,
    FOREIGN KEY (owner_id) REFERENCES app_user(id) ON DELETE CASCADE
);

CREATE TABLE enrollment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    enrollment_time DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    FOREIGN KEY (event_id) REFERENCES app_event(id) ON DELETE CASCADE,
    UNIQUE (user_id, event_id)
);