package com.clinicapp.models;

import androidx.annotation.IntDef;

import com.clinicapp.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

public class CameraPositions {
    public float zoom;
    public final @Positions
    int position;
    public final String title;
    public boolean isSelected;
    public static final int NO_OVERLAY = -1;

    @IntDef({Positions.RIGHT, Positions.FRONTAL, Positions.LEFT, Positions.CROWN, Positions.VERTEX,
            Positions.HAIR_VERTEX, Positions.HAIR_VERTEX_ZOOM, Positions.HAIR_CROWN,
            Positions.HAIR_CROWN_ZOOM, Positions.HAIR_FRONTAL, Positions.HAIR_FRONTAL_ZOOM,
            Positions.HAIR_RIGHT, Positions.HAIR_RIGHT_ZOOM, Positions.HAIR_OCCIPITAL,
            Positions.HAIR_OCCIPITAL_ZOOM, Positions.HAIR_LEFT, Positions.HAIR_LEFT_ZOOM,
            Positions.FACE_FRONT_MID_HEAD, Positions.FACE_FRONT_RIGHT_HEAD,
            Positions.FACE_FRONT_LEFT_HEAD,
            Positions.FACE_FRONT_UNDER_RIGHT_EYE,
            Positions.FACE_FRONT_UNDER_LEFT_EYE,
            Positions.FACE_RIGHT_UPPER_CHEEK,
            Positions.FACE_RIGHT_LOWER_CHEEK,
            Positions.FACE_LEFT_UPPER_CHEEK,
            Positions.FACE_LEFT_LOWER_CHEEK,
            Positions.FACE_CHIN

    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Positions {
        int RIGHT = 0,
                FRONTAL = 1,
                LEFT = 2,
                CROWN = 3,
                VERTEX = 4,
                HAIR_FRONTAL = 5,
                HAIR_FRONTAL_ZOOM = 6,

        HAIR_CROWN = 7,
                HAIR_CROWN_ZOOM = 8,
                HAIR_VERTEX = 9,
                HAIR_VERTEX_ZOOM = 10,
                HAIR_RIGHT = 11,
                HAIR_RIGHT_ZOOM = 12,
                HAIR_OCCIPITAL = 13,
                HAIR_OCCIPITAL_ZOOM = 14,
                HAIR_LEFT = 15,
                HAIR_LEFT_ZOOM = 16,

        FACE_FRONT_MID_HEAD = 17,
                FACE_FRONT_RIGHT_HEAD = 18,
                FACE_FRONT_LEFT_HEAD = 19,
                FACE_FRONT_UNDER_RIGHT_EYE = 20,
                FACE_FRONT_UNDER_LEFT_EYE = 21,
                FACE_RIGHT_UPPER_CHEEK = 22,
                FACE_RIGHT_LOWER_CHEEK = 24,
                FACE_LEFT_UPPER_CHEEK = 23,
                FACE_LEFT_LOWER_CHEEK = 25,
                FACE_CHIN = 26;

    }

    private CameraPositions(float zoom, @Positions int position, String title, boolean isSelected) {
        this.zoom = zoom;
        this.position = position;
        this.title = title;
        this.isSelected = isSelected;
    }

    public String getShootType() {
        switch (position) {
            case Positions.HAIR_LEFT:
            case Positions.HAIR_RIGHT:
            case Positions.HAIR_VERTEX:
            case Positions.HAIR_OCCIPITAL:
            case Positions.HAIR_CROWN:
            case Positions.HAIR_FRONTAL:
            case Positions.HAIR_LEFT_ZOOM:
            case Positions.HAIR_RIGHT_ZOOM:
            case Positions.HAIR_VERTEX_ZOOM:
            case Positions.HAIR_OCCIPITAL_ZOOM:
            case Positions.HAIR_CROWN_ZOOM:
            case Positions.HAIR_FRONTAL_ZOOM:
                return zoom > 0 ? "3x_closeup" : "closeup";
            default:
                return "portrait";
        }
    }

    public int getImage(boolean isMale) {
        switch (position) {
            case Positions.HAIR_LEFT:
            case Positions.HAIR_LEFT_ZOOM:
            case Positions.LEFT:
                return isMale ? R.drawable.male_head_left : R.drawable.female_head_left;
            case Positions.HAIR_RIGHT:
            case Positions.HAIR_RIGHT_ZOOM:
            case Positions.RIGHT:
                return isMale ? R.drawable.male_head_right : R.drawable.female_head_right;
            case Positions.HAIR_FRONTAL:
            case Positions.HAIR_FRONTAL_ZOOM:
            case Positions.HAIR_VERTEX:
            case Positions.HAIR_VERTEX_ZOOM:
            case Positions.HAIR_CROWN:
            case Positions.HAIR_CROWN_ZOOM:
            case Positions.CROWN:
                return isMale ? R.drawable.male_head_top : R.drawable.female_head_top;
            case Positions.VERTEX:
                return isMale ? R.drawable.male_head_back : R.drawable.female_head_back;
            case Positions.HAIR_OCCIPITAL:
                return isMale ? R.drawable.male_head_back : R.drawable.female_head_back;
            case Positions.HAIR_OCCIPITAL_ZOOM:
                return isMale ? R.drawable.male_head_back : R.drawable.female_head_back;
            case Positions.FRONTAL:
                return isMale ? R.drawable.male_head_frontal : R.drawable.female_head_frontal;
            case Positions.FACE_FRONT_MID_HEAD:
            case Positions.FACE_FRONT_RIGHT_HEAD:
            case Positions.FACE_FRONT_LEFT_HEAD:
                return isMale ? R.drawable.male_head_frontal_glo : R.drawable.female_head_frontal_glo;
            case Positions.FACE_FRONT_UNDER_LEFT_EYE:
                return isMale ? R.drawable.male_head_frontal_leye : R.drawable.female_head_frontal_leye;
            case Positions.FACE_FRONT_UNDER_RIGHT_EYE:
                return isMale ? R.drawable.male_head_frontal_reye : R.drawable.female_head_frontal_reye;
            case Positions.FACE_LEFT_LOWER_CHEEK:
                return isMale ? R.drawable.male_head_left_lower : R.drawable.female_head_left_lower;
            case Positions.FACE_LEFT_UPPER_CHEEK:
                return isMale ? R.drawable.male_head_left_upper : R.drawable.female_head_left_upper;
            case Positions.FACE_RIGHT_LOWER_CHEEK:
                return isMale ? R.drawable.male_head_right_upper : R.drawable.female_head_right_lower;
            case Positions.FACE_RIGHT_UPPER_CHEEK:
                return isMale ? R.drawable.male_head_right_lower : R.drawable.female_head_right_upper;
            default:
                return isMale ? R.drawable.male_head_frontal : R.drawable.female_head_frontal;
        }
    }

    public boolean isHeadVisible() {
        return this.position == Positions.HAIR_FRONTAL ||
                this.position == Positions.HAIR_FRONTAL_ZOOM ||
                this.position == Positions.HAIR_VERTEX ||
                this.position == Positions.HAIR_VERTEX_ZOOM ||
                this.position == Positions.HAIR_CROWN ||
                this.position == Positions.HAIR_CROWN_ZOOM;
    }

    public boolean isHairPosition() {
        return this.position > Positions.VERTEX;
    }

    public boolean isGloCloseUpPosition() {
        return this.position >= Positions.FACE_FRONT_MID_HEAD;
    }


    public boolean isRightHeadVisible() {
        return this.position == Positions.HAIR_RIGHT ||
                this.position == Positions.HAIR_RIGHT_ZOOM;
    }

    public boolean isLeftHeadVisible() {
        return this.position == Positions.HAIR_LEFT ||
                this.position == Positions.HAIR_LEFT_ZOOM;
    }


    public boolean isFrontalChecked() {
        return this.position == Positions.HAIR_FRONTAL ||
                this.position == Positions.HAIR_FRONTAL_ZOOM ||
                this.position == Positions.HAIR_OCCIPITAL_ZOOM ||
                this.position == Positions.HAIR_OCCIPITAL;
    }

    public boolean isVertexChecked() {
        return this.position == Positions.HAIR_VERTEX ||
                this.position == Positions.HAIR_VERTEX_ZOOM;
    }

    public boolean isCrownChecked() {
        return this.position == Positions.HAIR_CROWN ||
                this.position == Positions.HAIR_CROWN_ZOOM ||
                this.position == Positions.HAIR_LEFT ||
                this.position == Positions.HAIR_LEFT_ZOOM ||
                this.position == Positions.HAIR_RIGHT ||
                this.position == Positions.HAIR_RIGHT_ZOOM ||
                this.position == Positions.HAIR_OCCIPITAL_ZOOM ||
                this.position == Positions.HAIR_OCCIPITAL;
    }


    public String getText() {
        return title;
    }

    public boolean shouldShowMarker() {
        return this.position == Positions.HAIR_OCCIPITAL || this.position == Positions.HAIR_OCCIPITAL_ZOOM
                || (this.position >= Positions.HAIR_VERTEX && this.position <= Positions.HAIR_FRONTAL_ZOOM);
    }

    public String getFacePosition(int currentPos) {
        switch (currentPos) {
            case Positions.LEFT:
                return "left";
            case Positions.FACE_LEFT_UPPER_CHEEK:
                return "left_up";
            case Positions.FACE_LEFT_LOWER_CHEEK:
                return "left_down";
            case Positions.RIGHT:
                return "right";
            case Positions.FACE_RIGHT_UPPER_CHEEK:
                return "right_up";
            case Positions.FACE_RIGHT_LOWER_CHEEK:
                return "right_down";
            case Positions.FRONTAL:
                return "frontal";
            case Positions.FACE_FRONT_MID_HEAD:
                return "front_mid";
            case Positions.FACE_FRONT_UNDER_RIGHT_EYE:
                return "front_right";
            case Positions.FACE_FRONT_UNDER_LEFT_EYE:
                return "front_left";
            default:
                return "";
        }
    }

    public String getFaceAreaPosition(int currentPos) {
        switch (currentPos) {
            case Positions.FACE_FRONT_MID_HEAD:
                return "forehead_center";
            case Positions.FACE_FRONT_RIGHT_HEAD:
                return "forehead_right";
            case Positions.FACE_FRONT_LEFT_HEAD:
                return "forehead_left";
            case Positions.FACE_FRONT_UNDER_RIGHT_EYE:
                return "under_eye_right";
            case Positions.FACE_FRONT_UNDER_LEFT_EYE:
                return "under_eye_left";
            case Positions.FACE_RIGHT_UPPER_CHEEK:
                return "upper_cheek_right";
            case Positions.FACE_LEFT_UPPER_CHEEK:
                return "upper_cheek_left";
            case Positions.FACE_RIGHT_LOWER_CHEEK:
                return "lower_cheek_right";
            case Positions.FACE_LEFT_LOWER_CHEEK:
                return "lower_cheek_left";
            case Positions.FACE_CHIN:
                return "chin";
            default:
                return "";
        }
    }

    public int getOverlay() {
        switch (position) {
            case Positions.LEFT:
                return R.drawable.left_camera_overlay;
            case Positions.RIGHT:
                return R.drawable.right_camera_overlay;
            case Positions.CROWN:
                return R.drawable.crown_camera_overlay;
            case Positions.VERTEX:
                return R.drawable.ic_vertax_top;
            case Positions.FRONTAL:
                return R.drawable.ic_frontal_bottom;
            case Positions.FACE_FRONT_MID_HEAD:
            case Positions.FACE_FRONT_UNDER_LEFT_EYE:
            case Positions.FACE_FRONT_UNDER_RIGHT_EYE:
            case Positions.FACE_LEFT_LOWER_CHEEK:
            case Positions.FACE_LEFT_UPPER_CHEEK:
            case Positions.FACE_RIGHT_LOWER_CHEEK:
            case Positions.FACE_RIGHT_UPPER_CHEEK:
                return R.drawable.rect;
            default:
                return R.drawable.hair_camera_overlay;
        }
    }

    public static ArrayList<CameraPositions> getCameraPositionArray(ArrayList<Integer> positions) {
        ArrayList<CameraPositions> result = new ArrayList<>();
        for (@Positions int position : positions) {
            switch (position) {
                case Positions.RIGHT:
                    result.add(new CameraPositions(0, position, "Right", true));
                    break;
                case Positions.FRONTAL:
                    result.add(new CameraPositions(0, position, "Frontal", true));
                    break;
                case Positions.LEFT:
                    result.add(new CameraPositions(0, position, "Left", true));
                    break;
                case Positions.CROWN:
                    result.add(new CameraPositions(0, position, "Crown", true));
                    break;

                case Positions.VERTEX:
                    result.add(new CameraPositions(0, position, "Back", true));
                    break;
                case Positions.HAIR_VERTEX:
                    result.add(new CameraPositions(0, position, "Vertex", true));
                    break;
                case Positions.HAIR_VERTEX_ZOOM:
                    result.add(new CameraPositions(3f, position, "Vertex (Zoomed)", true));
                    break;
                case Positions.HAIR_CROWN:
                    result.add(new CameraPositions(0, position, "Crown", true));
                    break;
                case Positions.HAIR_CROWN_ZOOM:
                    result.add(new CameraPositions(3f, position, "Crown (Zoomed)", true));
                    break;
                case Positions.HAIR_FRONTAL:
                    result.add(new CameraPositions(0, position, "Frontal", true));
                    break;
                case Positions.HAIR_FRONTAL_ZOOM:
                    result.add(new CameraPositions(3f, position, "Frontal (Zoomed)", true));
                    break;
                case Positions.HAIR_RIGHT:
                    result.add(new CameraPositions(0, position, "Right", true));
                    break;
                case Positions.HAIR_RIGHT_ZOOM:
                    result.add(new CameraPositions(3f, position, "Right (Zoomed)", true));
                    break;
                case Positions.HAIR_OCCIPITAL:
                    result.add(new CameraPositions(0, position, "Occipital", true));
                    break;
                case Positions.HAIR_OCCIPITAL_ZOOM:
                    result.add(new CameraPositions(3f, position, "Occipital (Zoomed)", true));
                    break;
                case Positions.HAIR_LEFT:
                    result.add(new CameraPositions(0, position, "Left", true));
                    break;
                case Positions.HAIR_LEFT_ZOOM:
                    result.add(new CameraPositions(3f, position, "Left (Zoomed)", true));
                    break;
                case Positions.FACE_FRONT_MID_HEAD:
                    result.add(new CameraPositions(0, position, "Mid Forhead", true));
                    break;
                case Positions.FACE_FRONT_UNDER_RIGHT_EYE:
                    result.add(new CameraPositions(0, position, "Under right eye", true));
                    break;
                case Positions.FACE_FRONT_UNDER_LEFT_EYE:
                    result.add(new CameraPositions(0, position, "Under left eye", true));
                    break;
                case Positions.FACE_RIGHT_UPPER_CHEEK:
                    result.add(new CameraPositions(0, position, "Right upper cheek", true));
                    break;
                case Positions.FACE_RIGHT_LOWER_CHEEK:
                    result.add(new CameraPositions(0, position, "Right lower cheek", true));
                    break;
                case Positions.FACE_LEFT_UPPER_CHEEK:
                    result.add(new CameraPositions(0, position, "Left upper cheek", true));
                    break;
                case Positions.FACE_LEFT_LOWER_CHEEK:
                    result.add(new CameraPositions(0, position, "Left lower cheek", true));
                    break;
                case Positions.FACE_CHIN:
                    result.add(new CameraPositions(0, position, "Chin", true));
                    break;
                case Positions.FACE_FRONT_RIGHT_HEAD:
                    result.add(new CameraPositions(0, position, "Right forehead", true));
                    break;
                case Positions.FACE_FRONT_LEFT_HEAD:
                    result.add(new CameraPositions(0, position, "Left forehead", true));
                    break;
            }
        }
        return result;
    }

}