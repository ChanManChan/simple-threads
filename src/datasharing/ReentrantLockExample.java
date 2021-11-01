package datasharing;

import javafx.animation.AnimationTimer;
import javafx.animation.FillTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;
import javafx.util.Duration;

import javafx.beans.value.ChangeListener;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockExample extends Application {

    public static void main(String[] args) {
        // --module-path D:\programFiles\javafx-sdk-17.0.1\lib --add-modules=javafx.controls,javafx.fxml (VM options)
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Cryptocurrency Prices");

        GridPane grid = createGrid();
        Map<String, Label> cryptoLabels = createCryptoLabels();

        addLabelsToGrid(cryptoLabels, grid);

        double width = 300;
        double height = 250;

        StackPane root = new StackPane();
        Rectangle background = createBackgroundRectangleWithAnimation(width, height);
        root.getChildren().add(background);
        root.getChildren().add(grid);

        stage.setScene(new Scene(root, width, height));

        PricesContainer pricesContainer = new PricesContainer(); // shared by the UI and worker thread
        PriceUpdater priceUpdater = new PriceUpdater(pricesContainer);

        AnimationTimer animationTimer = new AnimationTimer() { // when added to the UI thread, calls the handle callback on every frame that is shown to the user
            @Override
            public void handle(long l) {
                // if the UI thread shows 30fps, this handle method is going to be called 30 times per second
                if (pricesContainer.getLockObject().tryLock()) { // using tryLock() we avoid blocking the real time thread and kept the application responsive
                    // pricesContainer.getLockObject().lock(); // makes the UI unresponsive and laggy use, tryLock() here instead
                    try {
                        Label bitcoinLabel = cryptoLabels.get("BTC");
                        bitcoinLabel.setText(String.valueOf(pricesContainer.getBitcoinPrice()));

                        Label etherLabel = cryptoLabels.get("ETH");
                        etherLabel.setText(String.valueOf(pricesContainer.getEtherPrice()));

                        Label litecoinLabel = cryptoLabels.get("LTC");
                        litecoinLabel.setText(String.valueOf(pricesContainer.getLitecoinPrice()));

                        Label bitcoinCashLabel = cryptoLabels.get("BCH");
                        bitcoinCashLabel.setText(String.valueOf(pricesContainer.getBitcoinCashPrice()));

                        Label rippleLabel = cryptoLabels.get("XRP");
                        rippleLabel.setText(String.valueOf(pricesContainer.getRipplePrice()));
                    } finally {
                        pricesContainer.getLockObject().unlock();
                    }
                }
            }
        };

        addWindowResizeListener(stage, background);

        animationTimer.start();
        priceUpdater.start();
        stage.show(); // all UI events, animations and rendering is happening on a single UI thread.
        // After we invoke the stage.show() method, the thread basically runs in an infinite loop responding to events, updating the screen many times per second.
        // Since the UI thread has to respond to events and show frames in real time, it must never be blocked or get suspended.
    }

    private void addWindowResizeListener(Stage stage, Rectangle background) {
        ChangeListener<Number> stageSizeListener = ((observable, oldValue, newValue) -> {
            background.setHeight(stage.getHeight());
            background.setWidth(stage.getWidth());
        });
        stage.widthProperty().addListener(stageSizeListener);
        stage.heightProperty().addListener(stageSizeListener);
    }

    private Rectangle createBackgroundRectangleWithAnimation(double width, double height) {
        Rectangle background = new Rectangle(width, height);
        FillTransition fillTransition = new FillTransition(Duration.millis(1000), background, Color.LIGHTGREEN, Color.LIGHTBLUE);
        fillTransition.setCycleCount(Timeline.INDEFINITE);
        fillTransition.setAutoReverse(true);
        fillTransition.play();
        return background;
    }

    private void addLabelsToGrid(Map<String, Label> labels, GridPane grid) {
        int row = 0;
        for (Map.Entry<String, Label> entry : labels.entrySet()) {
            String cryptoName = entry.getKey();
            Label nameLabel = new Label(cryptoName);
            nameLabel.setTextFill(Color.BLUE);
            nameLabel.setOnMousePressed(mouseEvent -> nameLabel.setTextFill(Color.RED));
            nameLabel.setOnMouseReleased(mouseEvent -> nameLabel.setTextFill(Color.BLUE));

            grid.add(nameLabel, 0, row);
            grid.add(entry.getValue(), 1, row);

            row++;
        }
    }

    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        return grid;
    }

    private Map<String, Label> createCryptoLabels() {
        Label bitcoinPrice = new Label("0");
        bitcoinPrice.setId("BTC");

        Label etherPrice = new Label("0");
        bitcoinPrice.setId("ETH");

        Label liteCoinPrice = new Label("0");
        bitcoinPrice.setId("LTC");

        Label bitcoinCashPrice = new Label("0");
        bitcoinPrice.setId("BCH");

        Label ripplePrice = new Label("0");
        bitcoinPrice.setId("XRP");

        Map<String, Label> cryptoLabelsMap = new HashMap<>();
        cryptoLabelsMap.put("BTC", bitcoinPrice);
        cryptoLabelsMap.put("ETH", etherPrice);
        cryptoLabelsMap.put("LTC", liteCoinPrice);
        cryptoLabelsMap.put("BCH", bitcoinCashPrice);
        cryptoLabelsMap.put("XRP", ripplePrice);

        return cryptoLabelsMap;
    }

    public static class PricesContainer {
        private Lock lockObject = new ReentrantLock();
        private double bitcoinPrice;
        private double etherPrice;
        private double litecoinPrice;
        private double bitcoinCashPrice;
        private double ripplePrice;

        public Lock getLockObject() {
            return lockObject;
        }

        public void setLockObject(Lock lockObject) {
            this.lockObject = lockObject;
        }

        public double getBitcoinPrice() {
            return bitcoinPrice;
        }

        public void setBitcoinPrice(double bitcoinPrice) {
            this.bitcoinPrice = bitcoinPrice;
        }

        public double getEtherPrice() {
            return etherPrice;
        }

        public void setEtherPrice(double etherPrice) {
            this.etherPrice = etherPrice;
        }

        public double getLitecoinPrice() {
            return litecoinPrice;
        }

        public void setLitecoinPrice(double litecoinPrice) {
            this.litecoinPrice = litecoinPrice;
        }

        public double getBitcoinCashPrice() {
            return bitcoinCashPrice;
        }

        public void setBitcoinCashPrice(double bitcoinCashPrice) {
            this.bitcoinCashPrice = bitcoinCashPrice;
        }

        public double getRipplePrice() {
            return ripplePrice;
        }

        public void setRipplePrice(double ripplePrice) {
            this.ripplePrice = ripplePrice;
        }
    }

    public static class PriceUpdater extends Thread {
        private final PricesContainer pricesContainer;
        private final Random random = new Random();

        public PriceUpdater(PricesContainer pricesContainer) {
            this.pricesContainer = pricesContainer;
        }

        @Override
        public void run() {
            while (true) {
                pricesContainer.getLockObject().lock();
                try {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    pricesContainer.setBitcoinPrice(random.nextInt(20_000));
                    pricesContainer.setEtherPrice(random.nextInt(2000));
                    pricesContainer.setLitecoinPrice(random.nextInt(500));
                    pricesContainer.setBitcoinCashPrice(random.nextInt(5000));
                    pricesContainer.setRipplePrice(random.nextDouble());
                } finally {
                    pricesContainer.getLockObject().unlock();
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
