package zepplins.zen.homelessassist.model;

/**
 * Gender options that user can filter by
 */
public enum Gender {
    MALE("Male"),
    FEMALE("Female"),
    ANYONE("Anyone");

    private final String displayName;

    Gender (String displayName){
        this.displayName = displayName;
    }

    @Override
    public String toString(){
        return displayName;
    }
}
