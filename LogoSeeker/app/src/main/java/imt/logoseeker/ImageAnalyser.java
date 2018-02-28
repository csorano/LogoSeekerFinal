package imt.logoseeker;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_calib3d;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_features2d;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacpp.opencv_shape;
import org.bytedeco.javacpp.opencv_xfeatures2d;

import java.util.ArrayList;
import java.util.Arrays;

import static org.bytedeco.javacpp.opencv_core.DMatch;
import static org.bytedeco.javacpp.opencv_core.DMatchVector;
import static org.bytedeco.javacpp.opencv_core.KeyPointVector;
import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.NORM_L2;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

class ImageAnalyser {
    private ArrayList<ObjectForImage> baseDonnee;

    ImageAnalyser(ArrayList<ObjectForImage> bdd)
    {
        this.baseDonnee = bdd;
    }

    // Reconnaissance javaCV
    String startRecognition(String photoPath)
    {
        ObjectForImage coca = new ObjectForImage();
        ObjectForImage pepsi = new ObjectForImage();
        ObjectForImage sprite = new ObjectForImage();

        for (int i = 0;i<baseDonnee.size();i++){
            if(baseDonnee.get(i).getName()=="coca")
                coca=baseDonnee.get(i);
            else if (baseDonnee.get(i).getName()=="pepsi")
                pepsi=baseDonnee.get(i);
            else if (baseDonnee.get(i).getName()=="sprite")
                sprite=baseDonnee.get(i);
        }

        Mat imagePri =	imread(photoPath);
        opencv_imgproc.resize(imagePri, imagePri, new opencv_core.Size(500, 700));

        Mat descriptorPri = new Mat();
        KeyPointVector	keyPointsPri	=	new	KeyPointVector();

        //Paramètre du SIFT.create
        int	nFeatures	=	0;
        int	nOctaveLayers	=	3;
        double	contrastThreshold	=	0.03;
        int	edgeThreshold	=	10;
        double	sigma	=	1.6;
        Loader.load(opencv_calib3d.class);
        Loader.load(opencv_shape.class)	;
        opencv_xfeatures2d.SIFT sift;
        sift= opencv_xfeatures2d.SIFT.create(nFeatures,	nOctaveLayers,	contrastThreshold,	edgeThreshold,	sigma);


        //Classe SIFT permettant de calculer les points où il y a des changements de texture, contraste sur l'image
        //Detection de l'image primaire
        sift.detect(imagePri,	keyPointsPri);
        sift.compute(imagePri,keyPointsPri,descriptorPri);

        opencv_features2d.BFMatcher matcher	=	new opencv_features2d.BFMatcher(NORM_L2,	false);
        DMatchVector	matches	=	new	DMatchVector();
        DMatchVector bestMatches;


        //Creer les matchs entre l'image Primaire et les autres images.Chaque match à une distance.On calculera la diastance moyenne.
        //On retourne la distance moyenne entre l'image primaire et chaque images de la classe Coca
        float[] DM1 = new float[3];
        for (int i=0;i<coca.getRef().length;i++){

            matcher.match(descriptorPri,coca.getDescriptors()[i],matches);
            bestMatches = selectBest (matches,25);
            DM1[i]=distanceMoyenne(bestMatches);
        }

        //On retourne la distance moyenne entre l'image primaire et chaque images de la classe Pepsi
        float[] DM2 = new float[3];
        for (int i=0;i<pepsi.getRef().length;i++){

            matcher.match(descriptorPri,	pepsi.getDescriptors()[i],	matches);
            bestMatches = selectBest (matches,25);
            //drawMatches(imagePri,	keyPointsPri,	descriptorsPepsi[i], keyPointsPepsi[i], bestMatches,	imageMatches);
            DM2[i]=distanceMoyenne(bestMatches);
        }

        //On retourne la distance moyenne entre l'image primaire et chaque images de la classe Sprite
        float[] DM3 = new float[3];
        for (int i=0;i<sprite.getRef().length;i++){

            matcher.match(descriptorPri,sprite.getDescriptors()[i],	matches);
            bestMatches = selectBest (matches,25);
            //drawMatches(imagePri,	keyPointsPri,	descriptorsSprite[i], keyPointsSprite[i], bestMatches,	imageMatches);
            DM3[i]=distanceMoyenne(bestMatches);
        }

        //On calcule la distance moyenne total de chaque classe

        float DMCoca=(DM1[0]+DM1[1]+DM1[2])/3;
        float DMPepsi=(DM2[0]+DM2[1]+DM2[2])/3;
        float DMSprite=(DM3[0]+DM3[1]+DM3[2])/3	;

        String res = "Not Found";
        //On affiche le nom de la marque ressemblant à l'image primaire
        if (DMCoca<DMPepsi && DMCoca<DMSprite){
            res = "Classe Coca";
            //Toast.makeText(MainActivity.this, "Classe Coca", Toast.LENGTH_SHORT).show();
        }

        else if (DMPepsi<DMCoca && DMPepsi<DMSprite){
            res = "Classe Pepsi";
            //Toast.makeText(MainActivity.this, "classe Pepsi", Toast.LENGTH_SHORT).show();
        }

        else if (DMSprite<DMCoca && DMSprite<DMPepsi){
            res = "Classe Sprite";
            //Toast.makeText(MainActivity.this, "classe Sprite", Toast.LENGTH_SHORT).show();
        }
        return res;
    }

    private DMatchVector	selectBest(DMatchVector	matches,	int numberToSelect)	{
        DMatch[]	sorted =	toArray(matches);
        Arrays.sort(sorted,	(a, b)	->	{
            return a.lessThan(b)	?	-1	:	1;
        });
        DMatch[]	best =	Arrays.copyOf(sorted,	numberToSelect);
        return new DMatchVector(best);
    }


    private DMatch[]	toArray(DMatchVector	matches)	{
        if (matches.size() > Integer.MAX_VALUE) throw new AssertionError();
        int	n	=	(int)	matches.size();


        //	Convert	keyPoints	to	Scala	sequence
        DMatch[]	result	=	new	DMatch[n];
        for	(int	i	=	0;	i	<	n;	i++)	{
            result[i]	=	new	DMatch(matches.get(i));
        }
        return	result;
    }

    //Methode pour calculer la distance moyenne des "numberToSelect" matcher
    private float distanceMoyenne(DMatchVector bestMatches){

        float DM=0.0f;
        for(int i=0;i<bestMatches.size();i++){

            DM+=bestMatches.get(i).distance();
        }

        DM=DM/bestMatches.size();
        return DM;

    }

}
