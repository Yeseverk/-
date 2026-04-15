package com.cjc;

import com.cjc.pojo.TbBrand;
import com.cjc.query.QueryParams;
import com.cjc.service.BrandService;
import com.cjc.util.PageList;
import com.cjc.vo.TbBrandVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TBBrandTest {

    @Autowired
    private BrandService brandService;

    @Test
    public void test1(){
        QueryParams<TbBrand> params = new QueryParams<>();
        params.setCurrentPage(1);
        params.setPageSize(5);
        TbBrand tbBrand = new TbBrand();
        tbBrand.setName("O");
        params.setParams(tbBrand);
        PageList<TbBrandVo> pageList = brandService.queryPage(params);
        System.out.println(pageList);
    }
}
