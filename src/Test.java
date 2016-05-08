import animator.interpolator.*;
import java.awt.Color;

/**
 *
 * @author cir5274
 */
public class Test {
    public static void main(String[] args) {
        javax.swing.JFrame window = new javax.swing.JFrame();
        window.setLayout(null);
        window.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        window.setSize(500, 500);
        window.setLocationRelativeTo(null);
        
        ALabel a = new ALabel();
        a.setText("");
        a.setImage("Trash.png");
        a.setScalingImage(true);
        a.setBounds(10, 17, 50, 50);
        a.setBackground(Color.red);
        a.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        a.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        //window.add(a);
        
        AComponent b = new AComponent();
        b.setBounds(400, 400, 50, 50);
        b.setBackground(Color.blue);
        window.add(b);
        
        window.setVisible(true);
        a.await(1000);
        a.setHighlightColor(Color.CYAN);
        
        animator.Animator.getInstance().setSyncing(true);
        animator.Animator.getInstance().setFramesPerSecond(60);
        a.lasting(350)
                .enter(window)
                .await()
                .using(new DeceleratingInterpolator())
                .translate(b.getX(), b.getY())
                .await()
                .translate(10, 319)
                .await()
                .translate(336, 234)
                .await()
                .translate(119, 400)
                .await()
                .translate(b.getX(), b.getY())
                .translate(250, 250)
                .await()
                .translate(10, 17)
                .await()
                .scaleWidth(100)
                .await()
                .scaleWidth(400)
                .await()
                .scaleWidth(10)
                .await()
                .scaleWidth(300)
                .await()
                .scaleWidth(50)
                .await()
                .scaleHeight(100)
                .await()
                .scaleHeight(400)
                .await()
                .scaleHeight(10)
                .await()
                .scaleHeight(300)
                .await()
                .scaleHeight(50)
                .await()
                .scale(100, 200)
                .await()
                .scale(52, 20)
                .await()
                .scale(250, 80)
                .await()
                .scale(200, 200)
                .await()
                .scale(50, 50)
                .await()
                .transform(200, 200, 200, 200)
                .await()
                .transform(300, 34, 120, 89)
                .await()
                .transform(35, 179, 350, 265)
                .await()
                .transform(b.getBounds())
                .await()
                .scaleWidthCentered(25)
                .await()
                .scaleWidthCentered(100)
                .await()
                .scaleHeightCentered(25)
                .await()
                .scaleHeightCentered(100)
                .await()
                .translate(200, 200)
                .await()
                .scaleCentered(25, 25)
                .await(100)
                .scaleCentered(200, 200)
                .await(200)
                .pulse(76, 115, 10)
                .await()
                .unpulse(76, 115)
                .await()
                .pulse()
                .await()
                .unpulse()
                .await(200)
                .washUp()
                .await()
                .unwashUp()
                .await()
                .washDown()
                .await()
                .unwashDown()
                .await()
                .washLeft()
                .await()
                .unwashLeft()
                .await()
                .washRight()
                .await()
                .unwashRight()
                .await()
                .fade()
                .await()
                .unpulse()
                .await()
                .fade()
                .await()
                .unfade()
                .await()
                .rotate(45)
                .await()
                .rotate(90)
                .await()
                .rotate(135)
                .await()
                .rotate(480)
                .await()
                .rotate(0)
                .await()
                .adjust(10)
                .await()
                .adjust(a.getWidth())
                .await()
                .adjust(0)
                .await();
        a.swapText("Not done yet.").await(200);
        a.swapText("Still not done yet.").await(200);
        a.swapText("Wait for it").await(200);
        a.swapText("Done")
                .await(200)
                .exit(window)
                .await();
        System.out.println("This should appear after the last animation.");
    }
}
