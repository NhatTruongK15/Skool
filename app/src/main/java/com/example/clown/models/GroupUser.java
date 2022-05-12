package com.example.clown.models;

import java.io.Serializable;
import java.util.List;

public class GroupUser implements Serializable {
    public String  id, name, image, token;
    public List<String> admin,membersId;
}
