package zepplins.zen.homelessassist.model;

/**
 * Created by mayhul on 3/5/18.
 */

public enum Gender {
    MALE("Male"),
    FEMALE("Female"),
    ANYONE("Anyone");

    private String displayName;

    private Gender (String displayName){
        this.displayName = displayName;
    }

    @Override
    public String toString(){
        return displayName;
    }
}
