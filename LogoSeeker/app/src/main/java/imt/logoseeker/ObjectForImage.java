package imt.logoseeker;

import static org.bytedeco.javacpp.opencv_core.KeyPointVector;
import static org.bytedeco.javacpp.opencv_core.Mat;

// Objet stockant les informations d'une image pour la comparaison
public class ObjectForImage{
    private String name;
    private Mat[] ref;
    private Mat[] descriptors;
    private KeyPointVector[] keyPoints;


    ObjectForImage(String startName,Mat[] startRef,Mat[] startDescriptors,KeyPointVector[] startKeyPoints){
        this.name=startName;
        this.ref=startRef;
        this.descriptors=startDescriptors;
        this.keyPoints=startKeyPoints;
    }
    ObjectForImage(){
        this.name=null;
        this.ref=null;
        this.descriptors=null;
        this.keyPoints=null;
    }
    public String getName(){
        return name;
    }
    Mat[] getRef(){
        return ref;
    }
    Mat[] getDescriptors(){
        return descriptors;
    }
    public KeyPointVector[] getKeyPoints(){
        return keyPoints;
    }
}
