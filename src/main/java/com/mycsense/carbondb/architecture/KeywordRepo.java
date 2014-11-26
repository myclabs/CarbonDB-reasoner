package com.mycsense.carbondb.architecture;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.mycsense.carbondb.domain.Keyword;

import java.util.HashMap;

public class KeywordRepo extends AbstractRepo {
    protected HashMap<String, Keyword> keywordsCache;

    public KeywordRepo(Model model) {
        super(model);
        keywordsCache = new HashMap<>();
    }

    public Keyword getKeyword(Resource keywordResource)
    {
        String keywordId = getId(keywordResource);
        if (!keywordsCache.containsKey(keywordId)) {
            Keyword keyword = new Keyword(keywordId);
            keyword.setLabel(getLabelOrURI(keywordResource));
            keywordsCache.put(keywordId, keyword);
        }
        return keywordsCache.get(keywordId);
    }
}
