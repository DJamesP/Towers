import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.random;

public class Main extends Application {

    private int rodCount = 3;
    private int ringCount = 5;

    private double orgSceneX, orgSceneY;
    private double orgTranslateX, orgTranslateY;
    private Group rods = new Group();
    private Stage thePrimaryStage;


    @Override
    public void start(Stage primaryStage) {

        thePrimaryStage = primaryStage;
        //winAnimation(primaryStage);
        gameIntro(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void gameIntro(Stage aGivenStage) {
        Group root = new Group();
        Scene scene = new Scene(root, 800, 600, Color.LIGHTBLUE);
        aGivenStage.setScene(scene);

        Button playGame = new Button("Play Towers");
        root.getChildren().add(playGame);
        playGame.setTranslateX(scene.getWidth() / 2 - 109.04 / 2);
        playGame.setTranslateY(scene.getHeight() * 1 / 5);

        playGame.setOnAction((ae) -> setUpGame(aGivenStage));
        aGivenStage.show();

    }

    public void resetGame(Stage aGivenStage) {
        rods = new Group();
        setUpGame(aGivenStage);
    }

    public void setUpGame(Stage aGivenStage) {

        double sceneWidth = 800;
        double sceneHeight = 600;

        double baseWidth = 600;
        double baseHeight = 40;

        double rodWidth = 30;
        double rodHeight = 300;

        Group root = new Group();
        Scene scene = new Scene(root, sceneWidth, sceneHeight, Color.LIGHTBLUE);
        aGivenStage.setScene(scene);

        Rectangle woodenbase = new Rectangle(baseWidth,baseHeight, Color.SADDLEBROWN);
        root.getChildren().add(woodenbase);
        woodenbase.setTranslateX(scene.getWidth() / 2 - woodenbase.getWidth() / 2);
        woodenbase.setTranslateY(scene.getHeight() * 4 / 5);

        //Group rods = new Group();
        root.getChildren().add(rods);
        double bigRingWidth = woodenbase.getWidth() / ( rodCount * 1.1d );
        double bigRingGapWidth = 0.1d * bigRingWidth;
        for ( int i=0; i<rodCount; i++) {
            Rod rod = new Rod(rodWidth, rodHeight, Color.DARKGRAY);
            rods.getChildren().add(rod);
            rod.setTranslateX(scene.getWidth() / 2 - woodenbase.getWidth()/2
                    + (bigRingWidth + bigRingGapWidth)/2 + i * (bigRingWidth + bigRingGapWidth));
            rod.setTranslateY(scene.getHeight() * 4/5 - rodHeight);
            if (((Integer)i).equals(0)) {
                rod.isStartRod = true;
                rod.setRingCount(ringCount);
            }
            else {
                rod.setRingCount(0);
                rod.setTopRingWidth(1e100d);
            };
        }

        for ( Node rod : rods.getChildren()) {
            Rod thisRod = (Rod) rod;
            if (thisRod.isStartRod) {
                double ringWidthInc = (bigRingWidth - rodWidth) / ((ringCount + 1) * 1.5d);
                double ringHeight = 0.9 * rodHeight / ringCount;
                double ringWidth = bigRingWidth;
                double nextRingY = scene.getHeight() * 4 / 5 - ringHeight;

                for (int i = 0; i < ringCount; i++) {
                    Ring ring = new Ring(ringWidth, ringHeight, Color.ORANGE,thisRod,false);
                    ring.setSizeRank(i);
                    root.getChildren().add(ring);
                    thisRod.addRing(ring);

                    //Initialize rings to all be on first rod.
                    ring.setTranslateX(scene.getWidth() / 2 - woodenbase.getWidth() / 2 +
                            +rodWidth / 2 + bigRingGapWidth / 2 + i * ringWidthInc);
                    ring.setTranslateY(nextRingY);

                    nextRingY -= 0.9 * rodHeight / ringCount;
                    ringWidth -= ringWidthInc * 2;

                    if (i+1==ringCount) {
                        ring.setIsTopRing(true);
                    }
                }
            }
        }
    }

    // The following class defines the "Rod" upon which the Rings may be placed.  It has a public group "rings"
    // that stores the nodes of the Rings currently on the Rod.
    private class Rod extends Rectangle {
        private boolean isStartRod;
        private int ringCount;
        private double topRingWidth;
        List<Ring> rings;

        public Rod(double w, double h, Color clr) {
            super(w,h,clr);
            rings = new ArrayList<>();
        }

        public void addRing(Ring thisRing) {
            rings.add(thisRing);
        }

        public void removeRing(Ring thisRing) {
            rings.remove(thisRing);
        }

        public int getRingCount() {
            return ringCount;
        }
        public void setRingCount(int myRingCount) {
            ringCount = myRingCount;
        }
        public double getTopRingWidth() {
            return topRingWidth;
        }
        public void setTopRingWidth(double myTopRingWidth) {
            topRingWidth = myTopRingWidth;
        }
    }

    private class Ring extends Rectangle {
        private boolean isTopRing;
        private int sizeRank;
        private Rod parentRod;

        public Ring(double w, double h, Color clr, Rod parentRod, boolean thisIsTopRing) {
            super(w,h,clr);
            isTopRing = thisIsTopRing;
            this.setStrokeType(StrokeType.INSIDE);
            this.setStrokeWidth(4);
            this.setStroke(Color.BLACK);
            this.setOnMousePressed(ringOnMousePressedEventHandler);
            this.setOnMouseDragged(ringOnMouseDraggedEventHandler);
            this.setOnMouseReleased(ringOnMouseReleasedEventHandler);
            this.parentRod = parentRod;

            if (isTopRing) {
                this.setOnMouseEntered((ae) -> this.setStroke(Color.RED));
                this.setOnMouseExited((ae) -> this.setStroke(Color.BLACK));
            }
        }

        public void setSizeRank(int rank) {
            sizeRank = rank;
        }

        public int getSizeRank() {
            return sizeRank;
        }

        public boolean getIsTopRing() {
            return isTopRing;
        }
        public void setIsTopRing(boolean thisIsTopRing) {
            isTopRing = thisIsTopRing;
            if (isTopRing) {
                this.setOnMouseEntered((ae) -> this.setStroke(Color.RED));
                this.setOnMouseExited((ae) -> this.setStroke(Color.BLACK));
            }
            else {
                this.setOnMouseEntered((ae) -> this.setStroke(Color.BLACK));
                this.setOnMouseExited((ae) -> this.setStroke(Color.BLACK));
            }
        }

        public boolean isOnRod(Rod thisRod) {

            double[] cornerTL = { this.getTranslateX(), this.getTranslateY() };
            double[] cornerTR = { cornerTL[0] + this.getWidth(), cornerTL[1]};
            double[] cornerBL = { cornerTL[0] , cornerTL[1] + this.getHeight()};

            boolean ringInHeightRange = ( cornerTL[1] < thisRod.getTranslateY() + thisRod.getHeight() &
                    cornerBL[1] > thisRod.getTranslateY());
            boolean ringInWidthRange = ( cornerTR[0] > thisRod.getTranslateX() &
                    cornerTL[0] < thisRod.getTranslateX() + thisRod.getWidth());

            return (ringInHeightRange & ringInWidthRange);
        }
    }

        //double[] cornerBR = { cornerTR[0] , cornerTL[1] + ((Rectangle)(t.getSource())).getHeight()};

    EventHandler<MouseEvent> ringOnMousePressedEventHandler =
            new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent t) {
                    orgSceneX = t.getSceneX();
                    orgSceneY = t.getSceneY();
                    orgTranslateX = ((Rectangle)(t.getSource())).getTranslateX();
                    orgTranslateY = ((Rectangle)(t.getSource())).getTranslateY();
                }
            };


    EventHandler<MouseEvent> ringOnMouseDraggedEventHandler =
            new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent t) {
                    double offsetX = t.getSceneX() - orgSceneX;
                    double offsetY = t.getSceneY() - orgSceneY;
                    double newTranslateX = orgTranslateX + offsetX;
                    double newTranslateY = orgTranslateY + offsetY;

                    if (((Ring)t.getSource()).getIsTopRing()) {
                        ((Rectangle) (t.getSource())).setTranslateX(newTranslateX);
                        ((Rectangle) (t.getSource())).setTranslateY(newTranslateY);
                    }
                }
            };

    EventHandler<MouseEvent> ringOnMouseReleasedEventHandler =
            new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent t) {

                    boolean onRod = false;
                    Rod newRod;
                    Rod oldParentRod;
                    Ring thisRing = (Ring) t.getSource();
                    for (Node rod : rods.getChildren()) {
                        if ((thisRing.isOnRod((Rod) rod))) {
                            newRod = (Rod) rod;
                            onRod = true;
                            if (newRod.rings.size() == 0) {
                                oldParentRod = thisRing.parentRod;
                                thisRing.parentRod.removeRing(thisRing);
                                thisRing.parentRod = newRod;
                                newRod.addRing(thisRing);
                                newRod.setRingCount(newRod.getRingCount() + 1);
                                thisRing.setTranslateX(newRod.getTranslateX() +
                                        0.5d * (newRod.getWidth() - thisRing.getWidth()));
                                thisRing.setTranslateY(newRod.getTranslateY()+newRod.getHeight()-thisRing.getHeight());
                                //Loop through remaining rings and set a new top ring.
                                double minY = 1e100d;
                                for (Ring oldRing : oldParentRod.rings) {
                                    if (oldRing.getTranslateY()<minY) {
                                        minY = oldRing.getTranslateY();
                                    }
                                }
                                if (oldParentRod.getRingCount() == 1) oldParentRod.setRingCount(0);
                                for (Ring oldRing : oldParentRod.rings) {
                                    if (oldRing.getTranslateY() == minY) {
                                        oldRing.setIsTopRing(true);
                                        oldParentRod.setRingCount(oldParentRod.getRingCount()-1);
                                        break;
                                    }
                                }

                            } else if (newRod.equals(thisRing.parentRod)) {
                                thisRing.setTranslateX(orgTranslateX);
                                thisRing.setTranslateY(orgTranslateY);
                            } else {

                                for (Node ringNode : newRod.rings) {
                                    Ring ring = (Ring) ringNode;
                                    if (ring.getIsTopRing() &&
                                            (ring.getWidth() > (thisRing.getWidth()))) {

                                        ring.setIsTopRing(false);
                                        oldParentRod = thisRing.parentRod;
                                        thisRing.parentRod.removeRing(thisRing);
                                        thisRing.parentRod = newRod;
                                        newRod.addRing(thisRing);
                                        newRod.setRingCount(newRod.getRingCount() + 1);
                                        thisRing.setTranslateX(ring.getTranslateX() +
                                                0.5d * (ring.getWidth() - thisRing.getWidth()));
                                        thisRing.setTranslateY(ring.getTranslateY() - ring.getHeight());
                                        //Loop through remaining rings and set a new top ring.
                                        double minY = 1e100d;
                                        for (Ring oldRing : oldParentRod.rings) {
                                            if (oldRing.getTranslateY()<minY) {
                                                minY = oldRing.getTranslateY();
                                            }
                                        }
                                        if (oldParentRod.getRingCount() == 1) oldParentRod.setRingCount(0);
                                        for (Ring oldRing : oldParentRod.rings) {
                                            if (oldRing.getTranslateY() == minY) {
                                                oldRing.setIsTopRing(true);
                                                oldParentRod.setRingCount(oldParentRod.getRingCount()-1);
                                                break;
                                            }
                                        }
                                        if (newRod.getRingCount() == ringCount) {
                                            winAnimation(thePrimaryStage);
                                        }
                                    } else {
                                        thisRing.setTranslateX(orgTranslateX);
                                        thisRing.setTranslateY(orgTranslateY);
                                    }
                                }
                            }
                            break;
                        }
                    }
                    if (!onRod) {
                        thisRing.setTranslateX(orgTranslateX);
                        thisRing.setTranslateY(orgTranslateY);
                    }
                    int iRod = 0;
                    for ( Node rod : rods.getChildren()) {
                        iRod++;
                        System.out.println("Rod: "+iRod+" , Ring Count: "+((Rod)rod).getRingCount());
                    }
                }
            };

    public void winAnimation(Stage aGivenStage) {
        Group root = new Group();
        Scene scene = new Scene(root, 800, 600, Color.BLACK);
        aGivenStage.setScene(scene);

        Group circles = new Group();
        for ( int i=0; i<30; i++) {
            Circle circle = new Circle(150, Color.web("white", 0.05));
            circle.setStrokeType(StrokeType.OUTSIDE);
            circle.setStroke(Color.web("white", 0.16));
            circle.setStrokeWidth(4);
            circles.getChildren().add(circle);
        }
        circles.setEffect(new BoxBlur(10, 10, 3));
        Label label = new Label("You win!");
        label.setFont(Font.font(100));
        label.setTextFill(Color.LIGHTGREEN);
        label.setTranslateX(200);
        label.setTranslateY(200);

        Rectangle colorGrad = new Rectangle(scene.getWidth(), scene.getHeight(),
                new LinearGradient(0f, 1f, 1f, 0f, true, CycleMethod.NO_CYCLE, new
                        Stop[]{
                        new Stop(0, Color.web("#f8bd55")),
                        new Stop(0.14, Color.web("#c0fe56")),
                        new Stop(0.28, Color.web("#5dfbc1")),
                        new Stop(0.43, Color.web("#64c2f8")),
                        new Stop(0.57, Color.web("#be4af7")),
                        new Stop(0.71, Color.web("#ed5fc2")),
                        new Stop(0.85, Color.web("#ef504c")),
                        new Stop(1, Color.web("#f2660f")),}));
        colorGrad.widthProperty().bind(scene.widthProperty());
        colorGrad.heightProperty().bind(scene.heightProperty());

        //root.getChildren().add(colorGrad);
        //root.getChildren().add(circles);

        Group blendModeGroup =
                new Group (new Group(new Rectangle(scene.getWidth(), scene.getHeight(),
                        Color.BLACK) , circles) , colorGrad);
        colorGrad.setBlendMode(BlendMode.OVERLAY);
        root.getChildren().add(blendModeGroup);

        Button stopButton = new Button("Restart");

        stopButton.setOnAction( (ae) -> resetGame(aGivenStage));
        stopButton.setTranslateX(scene.getWidth() / 2 - 109.04 / 2);
        stopButton.setTranslateY(scene.getHeight() * 4 / 5);

        root.getChildren().add(stopButton);
        root.getChildren().add(label);

        Timeline timeline = new Timeline();
        for (Node circle : circles.getChildren()) {
            timeline.getKeyFrames().addAll(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(circle.translateXProperty(), random() * 800),
                            new KeyValue(circle.translateYProperty(), random() * 600)
                    ),
                    new KeyFrame(new Duration(40000),
                            new KeyValue(circle.translateXProperty(), random() * 800),
                            new KeyValue(circle.translateYProperty(), random() * 600)
                    )
            );

        }

        timeline.play();

        aGivenStage.show();

    }

}
