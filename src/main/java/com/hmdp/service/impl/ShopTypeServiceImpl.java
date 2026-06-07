package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static com.hmdp.utils.RedisConstants.CACHE_SHOPTYPE_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 查询所有商铺类型
     *
     * @return 商铺类型列表
     */
    @Override
    public Result queryTypeList() {
        //从缓存中查询店铺列表
        String key = CACHE_SHOPTYPE_KEY;
        String shopTypeJson = stringRedisTemplate.opsForValue().get(key);

        //判断是否存在
        if (StrUtil.isNotBlank(shopTypeJson)){
            //存在返回
            List<ShopType> typeList = JSONUtil.toList(shopTypeJson, ShopType.class);
            return Result.ok(typeList);
        }

        //不存在，根据sort查询数据库
        List<ShopType> typeList = query().orderByAsc("sort").list();

        //数据库不存在返回错误
        if (typeList == null || typeList.isEmpty()){
            return Result.fail("店铺类型不存在");
        }

        //数据库存在写出redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(typeList));

        //返回
        return Result.ok(typeList);
    }
}
