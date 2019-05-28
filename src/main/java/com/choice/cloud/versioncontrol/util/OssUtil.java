package com.choice.cloud.versioncontrol.util;


import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.*;
import com.choice.cloud.versioncontrol.config.VersionConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OSS工具类
 * @author jty
 */
@Component
public class OssUtil {
    @Autowired
    private VersionConfiguration versionConfiguration;
    private Logger logger = LoggerFactory.getLogger(OssUtil.class);
	public static OssUtil ossUtil;
	@PostConstruct
	public void init() {
		ossUtil = this;
	}

	private static Map<String, String> cache = new HashMap<>();

	/**
	 * 将文件上传至oss
	 * @param file
	 * @param abucketName
	 * @param key 文件在oss中的key值
	 * @return
	 */
	public String upload(File file, String abucketName, String key) {
		if(!validateParam(file, abucketName)) {
			return null;
		}
		
        OSSClient ossClient = new OSSClient(versionConfiguration.getEndpoint(), versionConfiguration.getKey(), versionConfiguration.getSecret());
        
        String bucketName = abucketName;
//        String bucketName = getAccessBucketName(ossClient, abucketName);
        try {
			/**
			 * 判断传入key值指定的文件是否存在，存在则删除原有文件，生成一个新的key值
			 * 用于文件变更上传时返回不同的url
			 */
			boolean exists = ossClient.doesObjectExist(bucketName, key);
            if(exists) {
            	ossClient.deleteObject(bucketName, key);
            	//key = getNewKey(key);
			}

            logger.info("上传文件至oss开始，bucketName={},key={}" , bucketName ,key);
            PutObjectResult obj = ossClient.putObject(new PutObjectRequest(bucketName, key, file));
            
            exists = ossClient.doesObjectExist(bucketName, key);
            logger.info("文件上传" + (exists ? "成功" : "失败") + ".bucketName={},key={}", bucketName , key);
            
            ossClient.setObjectAcl(bucketName, key, CannedAccessControlList.PublicRead);

			String url = "http://" + bucketName + "." + versionConfiguration.getEndpoint().replace("http://", "") + "/" + key;
			return url;
        } catch (Exception e) {
            logger.error("文件上传至oss发生异常，bucketName={},key={},error={}" , bucketName , key, e);
            return null;
        } finally {
            ossClient.shutdown();
        }
    }


	


	private boolean validateParam(File file, String abucketName) {
		if(file == null) {
			return false;
		}
		if(!file.exists()) {
			return false;
		}
		if(StringUtils.isEmpty(abucketName)) {
			return false;
		}
		if(StringUtils.isEmpty(versionConfiguration.getEndpoint())) {
			return false;
		}
		if(StringUtils.isEmpty(versionConfiguration.getKey())) {
			return false;
		}
		if(StringUtils.isEmpty(versionConfiguration.getSecret())) {
			return false;
		}
		return true;
	}

	/**
	 * 获取bucketName
	 * @param client
	 * @param bucketName
	 * @return
	 */
	private static String getAccessBucketName(OSSClient client, String bucketName) {
		if (!cache.containsKey(bucketName)) {
			cache.put(bucketName, getMyBucketName(client, bucketName));
		}
		return cache.get(bucketName);
	}

	/**
	 * 根据bucketName生成当前账户可用的bucket
	 * 如不存在则创建
	 *
	 * @param client
	 * @param bucketName
	 * @return
	 */
	private synchronized static String getMyBucketName(OSSClient client, String bucketName) {
		int i = 0;
		String newBucket = bucketName;
		while (client.doesBucketExist(newBucket)) {
			if (isOwnBucket(client, newBucket)) {
				return newBucket;
			} else {
				i++;
			}
			newBucket = bucketName + "-" + i;
		}

		createNewBucket(client, newBucket);
		return newBucket;
	}

	/**
	 * 创建新的bucket
	 * @param client
	 * @param bucketName
	 */
	private static void createNewBucket(OSSClient client, String bucketName) {
		CreateBucketRequest request = new CreateBucketRequest(bucketName);
		request.setCannedACL(CannedAccessControlList.Private);
		client.createBucket(request);
	}

	/**
	 * 当前账户是否拥有指定bucket
	 *
	 * @param client
	 * @param bucketName
	 * @return
	 */
	private static boolean isOwnBucket(OSSClient client, String bucketName) {
		if (bucketName == null) {
			return false;
		}
		List<Bucket> bucketList = client.listBuckets();
		for (Bucket bucket : bucketList) {
			if (bucketName.equals(bucket.getName())) {
				return true;
			}
		}
		return false;
	}
}

