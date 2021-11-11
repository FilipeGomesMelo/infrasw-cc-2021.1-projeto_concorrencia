import ui.PlayerWindow;

import java.time.Duration;
import java.time.Instant;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class Player {

    private PlayerWindow playerWindow;
    private  boolean isActive = true;
    private boolean isPlaying = false;
    private boolean isRepeat = false;
    private double currentTime = 0;
    private int currentId = -1;
    private Instant start;
    private Instant stop;


    String [][] Queue = {
            {"Mr. Blue Sky", "Out Of The Blue", "Electric Light Orchestra", "1977", "05:02", "302", "0"},
            {"Carry On Wayward Son", "Leftoverture", "Kansas", "1976", "05:25", "325", "1"}
    };

    public Player() {
        ActionListener buttonListenerPlayNow = e -> start();

        ActionListener buttonListenerRemove = e -> {
            System.out.println("buttonListenerRemove");
        };
        ActionListener buttonListenerAddSong = e -> {
            System.out.println("buttonListenerAddSong");
        };
        ActionListener buttonListenerPlayPause = e -> {
            this.isPlaying = !this.isPlaying;
            this.playerWindow.updatePlayPauseButton(this.isPlaying);
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



        this.playerWindow = new PlayerWindow(
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

        while (true) {
            System.out.println(this.isPlaying);
            if (this.isPlaying) {
                if (this.currentTime > Integer.parseInt(Queue[this.currentId][5])) {
                    this.isPlaying = false;
                }

                this.stop = Instant.now();
                Duration dt = Duration.between(this.start, this.stop);

                this.playerWindow.updateMiniplayer(
                        this.isActive,
                        this.isPlaying,
                        this.isRepeat,
                        (int) this.currentTime,
                        Integer.parseInt(this.Queue[this.currentId][5]),
                        this.currentId,
                        this.Queue.length
                );
                if (dt.getSeconds() > 1) {
                    this.start = Instant.now();
                    this.currentTime += 2;
                }
            }
        }
    }

    public void start() {
        this.currentId = this.playerWindow.getSelectedSongID();
        this.currentTime = 0;
        this.playerWindow.updatePlayingSongInfo(this.Queue[currentId][0],
                this.Queue[currentId][1],
                this.Queue[currentId][2]);
        this.playerWindow.updateMiniplayer(
                this.isActive,
                this.isPlaying,
                this.isRepeat,
                (int) this.currentTime,
                Integer.parseInt(this.Queue[currentId][5]),
                this.currentId,
                this.Queue.length
                );
        this.playerWindow.enableScrubberArea();
        this.start = Instant.now();
        this.isPlaying = true;
        this.playerWindow.updatePlayPauseButton(this.isPlaying);
    }
}

