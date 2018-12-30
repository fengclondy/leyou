package com.leyou.search.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("item-service")
public interface SpecificationClient extends com.leyou.item.api.SpecificationApi{
}
