package com.choice.cloud.versioncontrol.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 配置信息
 * @author jty
 */
@Component
@Data
public class VersionConfiguration {
    @Value("${oss.endpoint}")
    private String endpoint;
    @Value("${oss.bucketPrefix}")
    private String bucketPrefix;
    @Value("${oss.key}")
    private String key;
    @Value("${oss.secret}")
    private String secret;
}
