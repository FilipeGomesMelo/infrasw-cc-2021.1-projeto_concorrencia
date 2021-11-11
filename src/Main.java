import ui.PlayerWindow;

import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class Main {
    public static void main(String[] args) {
        ActionListener buttonListenerPlayNow = e -> {
            System.out.println("buttonListPlayNow");
        };
        ActionListener buttonListenerRemove = e -> {
            System.out.println("buttonListenerRemove");
        };
        ActionListener buttonListenerAddSong = e -> {
            System.out.println("buttonListenerAddSong");
        };
        ActionListener buttonListenerPlayPause = e -> {
            System.out.println("buttonListenerPlayPause");
        };
        ActionListener buttonListenerStop = e -> {
            System.out.println("buttonListenerStop");
        };
        ActionListener buttonListenerNext = e -> {
            System.out.println("buttonListenerNext");
        };
        ActionListener buttonListenerPrevious = e -> {
            System.out.println("buttonListenerPrevious");
        };
        ActionListener buttonListenerShuffle = e -> {
            System.out.println("buttonListenerShuffle");
        };
        ActionListener buttonListenerRepeat = e -> {
            System.out.println("buttonListenerRepeat");
        };
        MouseListener scrubberListenerClick = new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("mouseClicked");
            }

            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println("mousePressed");
            }

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        };

        MouseMotionListener scrubberListenerMotion = new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                System.out.println("mouseDragged");
            }

            @Override
            public void mouseMoved(MouseEvent e) {}
        };

        String [][] Queue = {
                {"Mr. Blue Sky", "Out Of The Blue", "Electric Light Orchestra", "1977", "05:02", "302", "01"},
                {"Carry On Wayward Son", "Leftoverture", "Kansas", "1976", "05:25", "325", "02"}
        };

        PlayerWindow playerWindow = new PlayerWindow(
                buttonListenerPlayNow,
                buttonListenerRemove,
                buttonListenerAddSong,
                buttonListenerPlayPause,
                buttonListenerStop,
                buttonListenerNext,
                buttonListenerPrevious,
                buttonListenerShuffle,
                buttonListenerRepeat,
                scrubberListenerClick,
                scrubberListenerMotion,
                "Tittle",
                Queue
        );

        playerWindow.start();
    }
}
