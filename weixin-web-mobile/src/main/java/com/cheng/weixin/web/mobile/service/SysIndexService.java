package com.cheng.weixin.web.mobile.service;

import com.cheng.weixin.rpc.cart.service.RpcCartService;
import com.cheng.weixin.rpc.item.entity.Product;
import com.cheng.weixin.rpc.item.service.RpcProductService;
import com.cheng.weixin.rpc.system.entity.Ad;
import com.cheng.weixin.rpc.system.entity.Notice;
import com.cheng.weixin.rpc.system.service.RpcSystemService;
import com.cheng.weixin.web.mobile.exception.ProductException;
import com.cheng.weixin.web.mobile.exception.message.StatusCode;
import com.cheng.weixin.web.mobile.result.index.Index;
import com.cheng.weixin.web.mobile.result.index.IndexAd;
import com.cheng.weixin.web.mobile.result.index.IndexNotice;
import com.cheng.weixin.web.mobile.result.index.IndexProduct;
import com.cheng.weixin.web.mobile.security.LocalUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Desc:
 * Author: cheng
 * Date: 2016/6/28
 */
@Service("sysIndexService")
public class SysIndexService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private RpcCartService cartService;
    @Autowired
    private RpcProductService productService;
    @Autowired
    private RpcSystemService systemService;

    public Index getIndexInfo() {
        // 图片
        List<IndexAd> indexads = new ArrayList<>();
        List<Ad> ads = systemService.getIndexAds();
        for (Ad ad : ads) {
            IndexAd indexAd = new IndexAd();
            indexAd.setName(ad.getName());
            indexAd.setPictureUrl(ad.getPictureUrl());
            indexAd.setLinkUrl(ad.getLinkUrl());
            indexAd.setHeight(ad.getHeight());
            indexAd.setWidth(ad.getWidth());
            indexads.add(indexAd);
        }
        // 通告
        List<IndexNotice> indexNotices = new ArrayList<>();
        List<Notice> notices = systemService.getIndexNotices();
        for (Notice notice : notices) {
            IndexNotice indexNotice = new IndexNotice();
            indexNotice.setId(notice.getId());
            indexNotice.setContent(notice.getContent());
            indexNotice.setLinkUrl(notice.getLinkUrl());
            indexNotices.add(indexNotice);
        }
        // 商品
        List<IndexProduct> indexProducts = new ArrayList<>();
        List<Product> products = productService.getIndex();
        for (Product product : products) {
            IndexProduct indexProduct = new IndexProduct();
            indexProduct.setId(product.getId());
            indexProduct.setName(product.getName());
            indexProduct.setPrice(product.getSalePrice());
            indexProduct.setPicLink(product.getDefaultPicture().getPictureUrl());
            indexProduct.setHeight(product.getDefaultPicture().getHeight());
            indexProduct.setWidth(product.getDefaultPicture().getWidth());
            indexProduct.setTag(product.getTag());
            indexProduct.setDesc(product.getUnitDesc());
            indexProducts.add(indexProduct);
        }

        Index index = new Index();
        index.setAds(indexads);
        index.setNotices(indexNotices);
        index.setProducts(indexProducts);

        return index;
    }

    /**
     * 购买商品
     * @param productId
     * @return
     */
    public long addProduct(String productId) {
        Product product = productService.getById(productId);
        if (product.getUnitsInStock()<=0) {
            throw new ProductException(StatusCode.PRODUCT_STOCK_SHORTAGE);
        }
        long count = cartService.addProductCount(LocalUser.getUser().getUserId(), productId);
        if (product.getUnitsInStock() < count) {
            cartService.subProductCount(LocalUser.getUser().getUserId(), productId);
            throw new ProductException(StatusCode.PRODUCT_STOCK_SHORTAGE);
        }
        return count;
    }
    /**
     * 减少商品
     * @param productId
     * @return
     */
    public long subProduct(String productId) {
        long count = cartService.subProductCount(LocalUser.getUser().getUserId(), productId);
        return count;
    }

}
