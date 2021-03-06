package com.kthcorp.cmts.mapper;

import com.kthcorp.cmts.model.MetaKeywordMapping;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@MybatisTest
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class MetaKeywordMappingMapperTest {
    @Autowired
    private MetaKeywordMappingMapper metaKeywordMappingMapper;

    @Test
    @Rollback(false)
    public void test_getMetaKeywordMapping() throws Exception {
        MetaKeywordMapping req = new MetaKeywordMapping();
        req.setType("genre");

        List<MetaKeywordMapping> result = metaKeywordMappingMapper.getMetaKeywordMappingList(req);
        System.out.println("#iresult:" + result.toString());
    }
}
