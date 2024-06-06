package com.clinicapp.ui.home.viewmodels;

import android.app.Application;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.clinicapp.R;
import com.clinicapp.models.CameraPositions;
import com.clinicapp.models.Patient;
import com.clinicapp.utilities.BaseViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;

public class MainViewModel extends BaseViewModel {

    private List<Patient> searchResults = new ArrayList<>();
    private Patient selectedPatient;
    private long hairAnalysisID, faceAnalysisID;
    private ArrayList<CameraPositions> selectedPositions = new ArrayList<>();
    private ArrayList<String> imagesList = new ArrayList<>();
    private List<MultipartBody.Part> alImg = new ArrayList<>();
    private List<MultipartBody.Part> alMainType = new ArrayList<>();
    private List<MultipartBody.Part> alSubType = new ArrayList<>();
    private List<Long> alAnalysisID = new ArrayList<>();

    private ArrayList<MultipartBody.Part> imagesListFace = new ArrayList<>();
    private List<MultipartBody.Part> positionListFace = new ArrayList<>();
    private List<MultipartBody.Part> areaListFace = new ArrayList<>();
    private List<MultipartBody.Part> typeListFace = new ArrayList<>();
    private List<MultipartBody.Part> xAxisListFace = new ArrayList<>();
    private List<MultipartBody.Part> yAxisListFace = new ArrayList<>();
    private List<MultipartBody.Part> brownList = new ArrayList<>();
    private List<MultipartBody.Part> redList = new ArrayList<>();
    private List<MultipartBody.Part> poresList = new ArrayList<>();

    private List<Long> alFaceAnalysisID = new ArrayList<>();
    private List<String> faceTypes = new ArrayList<>();

    public ArrayList<CameraPositions> selectedFacePositions = new ArrayList<>();
    public ArrayList<CameraPositions> selectedCloseupFacePositions = new ArrayList<>();

    public ArrayList<String> getImagesList() {
        return imagesList;
    }

    public void setImagesList(ArrayList<String> imagesList) {
        this.imagesList = imagesList;
    }

    public void addImage(String path) {
        imagesList.add(path);
    }

    public void addDataParts(MultipartBody.Part image, MultipartBody.Part mainType, MultipartBody.Part subType, long hairAnalysisID) {
        alImg.add(image);
        alMainType.add(mainType);
        alSubType.add(subType);
        alAnalysisID.add(hairAnalysisID);
    }

    public void removeDataParts() {
        imagesList.removeAll(imagesList);
        alImg.removeAll(alImg);
        alMainType.removeAll(alMainType);
        alSubType.removeAll(alSubType);
        alAnalysisID.removeAll(alAnalysisID);
    }

    public void addFaceDataParts(MultipartBody.Part image, MultipartBody.Part type, MultipartBody.Part position,
                                 MultipartBody.Part area, MultipartBody.Part xaxis, MultipartBody.Part yaxis,
                                 MultipartBody.Part brown, MultipartBody.Part red, MultipartBody.Part pores,
                                 String faceType, long hairAnalysisID) {
        imagesListFace.add(image);
        typeListFace.add(type);
        positionListFace.add(position);
        areaListFace.add(area);
        alFaceAnalysisID.add(hairAnalysisID);
        faceTypes.add(faceType);
        xAxisListFace.add(xaxis);
        yAxisListFace.add(yaxis);
        brownList.add(brown);
        redList.add(red);
        poresList.add(pores);
    }

    public void removeFaceDataParts() {
        imagesList.removeAll(imagesList);
        imagesListFace.removeAll(imagesListFace);
        typeListFace.removeAll(typeListFace);
        positionListFace.removeAll(positionListFace);
        areaListFace.removeAll(areaListFace);
        alFaceAnalysisID.removeAll(alFaceAnalysisID);
        faceTypes.removeAll(faceTypes);
        xAxisListFace.removeAll(xAxisListFace);
        yAxisListFace.removeAll(yAxisListFace);
        brownList.removeAll(brownList);
        redList.removeAll(redList);
        poresList.removeAll(poresList);
    }

    public List<String> getFaceTypes() {
        return faceTypes;
    }

    public void setFaceTypes(List<String> faceTypes) {
        this.faceTypes = faceTypes;
    }

    public List<MultipartBody.Part> getAlImg() {
        return alImg;
    }

    public void setAlImg(List<MultipartBody.Part> alImg) {
        this.alImg = alImg;
    }

    public List<MultipartBody.Part> getAlMainType() {
        return alMainType;
    }

    public void setAlMainType(List<MultipartBody.Part> alMainType) {
        this.alMainType = alMainType;
    }

    public List<MultipartBody.Part> getAlSubType() {
        return alSubType;
    }

    public void setAlSubType(List<MultipartBody.Part> alSubType) {
        this.alSubType = alSubType;
    }

    public List<Long> getAlAnalysisID() {
        return alAnalysisID;
    }

    public void setAlAnalysisID(List<Long> alAnalysisID) {
        this.alAnalysisID = alAnalysisID;
    }

    public long getHairAnalysisID() {
        return hairAnalysisID;
    }

    public void setHairAnalysisID(long hairAnalysisID) {
        this.hairAnalysisID = hairAnalysisID;
    }

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    public void onPatientAdded(Patient patient) {
        this.selectedPatient = patient;
    }

    public void setSearchResult(List<Patient> patients) {
        this.searchResults = patients;
    }

    public List<Patient> getSearchResults() {
        return searchResults;
    }

    public Patient getSelectedPatient() {
        return selectedPatient;
    }

    public void setSelectedPatient(Patient selectedPatient) {
        this.selectedPatient = selectedPatient;
    }

    public void setCameraPositions(ArrayList<CameraPositions> selectedPositions) {
        this.selectedPositions = selectedPositions;
    }

    public ArrayList<CameraPositions> getSelectedCloseupFacePositions() {
        return selectedCloseupFacePositions;
    }

    public void setSelectedCloseupFacePositions(ArrayList<CameraPositions> selectedCloseupFacePositions) {
        this.selectedCloseupFacePositions = selectedCloseupFacePositions;
    }

    public ArrayList<CameraPositions> getSelectedPositions() {
        return selectedPositions;
    }

    public ArrayList<CameraPositions> getSelectedFacePositions() {
        return selectedFacePositions;
    }

    public void setSelectedFacePositions(ArrayList<CameraPositions> selectedFacePositions) {
        this.selectedFacePositions = selectedFacePositions;
    }

    public long getFaceAnalysisID() {
        return faceAnalysisID;
    }

    public void setFaceAnalysisID(long faceAnalysisID) {
        this.faceAnalysisID = faceAnalysisID;
    }

    public void removeOldImages() {
        String path = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            path = getApplication().getApplicationContext().getExternalCacheDirs() + "/" + getApplication().getApplicationContext().getResources().getString(R.string.imageFolder);

        } else {
            path = Environment.getExternalStorageDirectory() + "/" + getApplication().getApplicationContext().getResources().getString(R.string.imageFolder);

        }
        try {

            File directory = new File(path);
            if (directory.exists()) {
                File[] files = directory.listFiles();
                if (files.length > 0) {
                    for (int i = 0; i < files.length; i++) {
                        long msDiff = Calendar.getInstance().getTimeInMillis() - files[i].lastModified();
                        long daysDiff = TimeUnit.MILLISECONDS.toDays(msDiff);
                        if (daysDiff > 7) {
                            if (files[i].exists()) {
                                if (files[i].delete()) {
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    public void setSearchResults(List<Patient> searchResults) {
        this.searchResults = searchResults;
    }

    public void setSelectedPositions(ArrayList<CameraPositions> selectedPositions) {
        this.selectedPositions = selectedPositions;
    }

    public ArrayList<MultipartBody.Part> getImagesListFace() {
        return imagesListFace;
    }

    public void setImagesListFace(ArrayList<MultipartBody.Part> imagesListFace) {
        this.imagesListFace = imagesListFace;
    }

    public List<MultipartBody.Part> getPositionListFace() {
        return positionListFace;
    }

    public void setPositionListFace(List<MultipartBody.Part> positionListFace) {
        this.positionListFace = positionListFace;
    }

    public List<MultipartBody.Part> getAreaListFace() {
        return areaListFace;
    }

    public void setAreaListFace(List<MultipartBody.Part> areaListFace) {
        this.areaListFace = areaListFace;
    }

    public List<MultipartBody.Part> getTypeListFace() {
        return typeListFace;
    }

    public void setTypeListFace(List<MultipartBody.Part> typeListFace) {
        this.typeListFace = typeListFace;
    }

    public List<Long> getAlFaceAnalysisID() {
        return alFaceAnalysisID;
    }

    public void setAlFaceAnalysisID(List<Long> alFaceAnalysisID) {
        this.alFaceAnalysisID = alFaceAnalysisID;
    }

    public List<MultipartBody.Part> getxAxisListFace() {
        return xAxisListFace;
    }

    public void setxAxisListFace(List<MultipartBody.Part> xAxisListFace) {
        this.xAxisListFace = xAxisListFace;
    }

    public List<MultipartBody.Part> getyAxisListFace() {
        return yAxisListFace;
    }

    public void setyAxisListFace(List<MultipartBody.Part> yAxisListFace) {
        this.yAxisListFace = yAxisListFace;
    }

    public List<MultipartBody.Part> getBrownList() {
        return brownList;
    }

    public void setBrownList(List<MultipartBody.Part> brownList) {
        this.brownList = brownList;
    }

    public List<MultipartBody.Part> getRedList() {
        return redList;
    }

    public void setRedList(List<MultipartBody.Part> redList) {
        this.redList = redList;
    }

    public List<MultipartBody.Part> getPoresList() {
        return poresList;
    }

    public void setPoresList(List<MultipartBody.Part> poresList) {
        this.poresList = poresList;
    }
}
