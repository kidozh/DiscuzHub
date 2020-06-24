package com.kidozh.discuzhub.results;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class UserFriendResult extends BaseResult {
    @JsonProperty("Variables")
    public FriendVariables friendVariables;

    public static class FriendVariables extends VariableResults {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int count = 0;

        @JsonProperty("list")
        public List<UserFriend> friendList = null;
    }

    public static class UserFriend {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int uid = 0;

        public String username = "";
    }

    public boolean isError(){
        return message !=null ;
    }
}
