package com.kthcorp.cmts.mapper;

import com.kthcorp.cmts.model.Items;
import com.kthcorp.cmts.model.ItemsContent;
import com.kthcorp.cmts.model.ItemsTags;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface ItemsMapper {
    List<Items> getItems();
    List<Items> getPagedItems(Items req);
    Items getItemsByIdx(Items req);
    Items getItemsInfoByIdx(int itemidx);
    Integer countItems(Items req);

    int insItems(Items req);
    int uptItems(Items req);
    int delItems(Items req);

    List<ItemsContent> getItemsContent();
    List<ItemsContent> getItemsCine21Second520();

    int getCollectSchedId(int sc_id);
    List<ItemsContent> getCollectContentList(int sc_id);
    List<ItemsContent> getItemsCine21SecondFor10AndContentall();
    List<ItemsContent> getItemsYj01by01();
    List<ItemsContent> getItemsYj04();

    int insYjItemsOut(Items req);
    int insYjItemsOut2(ItemsTags req);
    int insYjTagsMetas(ItemsTags req);
    Items getYjidForInsert(Items req);

    List<ItemsContent> getItemsCine21();
    ItemsContent getMovieCine21ByTitle(String title);

    List<Items> searchItemsPaging(Items req);
    List<Items> searchListItemsPaging(Map<String,Object> map);
    // 승인완료,승인불가 조회를 위한 쿼리
    List<Items> searchItemsPagingByStatDate(Items req);

    List<Items> countItemsPagingByStat(Items req);
    int countItemsPaging(Items req);
    int countListItemsPaging(Map<String,Object> map);
    int countItemsAll();
    List<Items> searchTestMetas(Items req);


    int getScidByItemIdxAndType(Items req);
    int uptSchedTriggerStatByScid(Items req);
    int insItemsStat(Items req);
    int uptItemsTagcnt(Items req);
    int uptItemsTagcntMinus(Items req);
    int uptItemsRegdate(Items req);
    String getItemsStatByIdx(int idx);

    List<Map<String,Object>> getItemsInfoForPrism(Map<String,Object> req);

    //mcid로 동일 컨텐츠 검색
    List<Items> getItemListSameMcid(Items reqIt);
}
