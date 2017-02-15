package org.utm.pad.data_access_layer;


import org.utm.pad.model_layer.Article;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public interface DataAccess {

    CopyOnWriteArrayList<Article> getAllData();

    void updateEntry(int id, String content);

    void createEntry(String article);

    void deleteEntry(int id);
}
