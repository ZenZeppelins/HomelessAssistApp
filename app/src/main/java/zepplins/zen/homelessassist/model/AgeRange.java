package zepplins.zen.homelessassist.model;

/**
 * Created by mayhul on 3/5/18.
 */

public enum AgeRange {
    FAMILIES("Family"),
    CHILDREN("Children"),
    YOUNG_ADULTS("Young Adult"),
    ANYONE("Anyone");

    private String displayName;

    private AgeRange (String displayName){
        this.displayName = displayName;
    }

    @Override
    public String toString(){
        return displayName;
    }

}
