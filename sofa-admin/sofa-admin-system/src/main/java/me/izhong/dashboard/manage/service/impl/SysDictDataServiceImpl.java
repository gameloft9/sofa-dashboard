package me.izhong.dashboard.manage.service.impl;

import lombok.extern.slf4j.Slf4j;
import me.izhong.dashboard.manage.dao.DictDataDao;
import me.izhong.dashboard.manage.entity.SysDictData;
import me.izhong.dashboard.manage.domain.PageRequest;
import me.izhong.dashboard.manage.domain.PageModel;
import me.izhong.dashboard.manage.expection.BusinessException;
import me.izhong.dashboard.manage.service.CrudBaseService;
import me.izhong.dashboard.manage.service.SysDictDataService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class SysDictDataServiceImpl extends CrudBaseServiceImpl<Long,SysDictData> implements SysDictDataService {

    @Autowired
    private DictDataDao dictDataDao;

    @Override
    public List<SysDictData> selectDictDataByType(String dictType) {
        return dictDataDao.findAllByDictTypeOrderByDictSortAsc(dictType);
    }

    @Override
    public String selectDictLabel(String dictType, String dictValue) {
        SysDictData sysDictData = dictDataDao.findByDictTypeAndDictValue(dictType, dictValue);
        if (sysDictData != null) {
            return sysDictData.getDictLabel();
        }
        return "";
    }

}
