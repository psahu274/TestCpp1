package com.example.prashant.testcpp1;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Prashant on 17-03-2018.
 */

public class Note {
    public String title = null;
    public String description = null;
    public boolean idea = false;
    public boolean todo = false;
    public boolean important = false;

    public Note(){
    }

    public Note(String title,String description, boolean idea,boolean todo,boolean important){
        this.title = title;
        this.description = description;
        this.idea = idea;
        this.todo = todo;
        this.important = important;
    }

    public Map<String,Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("description",description);
        result.put("idea",idea);
        result.put("important",important);
        result.put("title",title);
        result.put("todo",todo);
        return result;
    }

}
