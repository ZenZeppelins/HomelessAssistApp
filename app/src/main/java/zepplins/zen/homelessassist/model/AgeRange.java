package zepplins.zen.homelessassist.model;

/**
 * Different age ranges user can filter by
 */
public enum AgeRange {
    FAMILIES("Family"),
    CHILDREN("Children"),
    YOUNG_ADULTS("Young Adult"),
    ANYONE("Anyone");

    private final String displayName;

    AgeRange (String displayName){
        this.displayName = displayName;
    }

    @Override
    public String toString(){
        return displayName;
    }

}
