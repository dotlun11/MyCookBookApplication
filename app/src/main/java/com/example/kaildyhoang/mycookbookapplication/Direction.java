package com.example.kaildyhoang.mycookbookapplication;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Direction {
    private String directionCont;
    private String directionIllustrationPicture;
    private int directionOrdinalNumber;

    public Direction(){}
    public Direction(int directionOrdinalNumber, String directionCont, String directionIllustrationPicture) {
        this.directionOrdinalNumber = directionOrdinalNumber;
        this.directionCont = directionCont;
        this.directionIllustrationPicture = directionIllustrationPicture;
    }
    public Direction(String directionCont, String directionIllustrationPicture) {
        this.directionCont = directionCont;
        this.directionIllustrationPicture = directionIllustrationPicture;
    }
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("directionCont", directionCont);
        result.put("directionIllustrationPicture", directionIllustrationPicture);

        return result;
    }
    public int getDirectionOrdinalNumber() {
        return directionOrdinalNumber;
    }

    public void setDirectionOrdinalNumber(int directionOrdinalNumber) {
        this.directionOrdinalNumber = directionOrdinalNumber;
    }

    public String getDirectionCont() {
        return directionCont;
    }

    public void setDirectionCont(String directionCont) {
        this.directionCont = directionCont;
    }

    public String getDirectionIllustrationPicture() {
        return directionIllustrationPicture;
    }

    public void setDirectionIllustrationPicture(String directionIllustrationPicture) {
        this.directionIllustrationPicture = directionIllustrationPicture;
    }

}
