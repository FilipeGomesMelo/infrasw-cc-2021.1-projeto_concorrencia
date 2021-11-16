import ui.AddSongWindow;
import ui.PlayerWindow;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Player {

    private PlayerWindow playerWindow;
    private  boolean isActive = true;
    private boolean isPlaying = false;
    private boolean isRepeat = false;
    private double currentTime = 0;
    private int currentId = -1;
    private int idCounter = 0;
    private Instant start;
    private Instant stop;

    private AddSongWindow addSongWindow = null;

    private final Lock lock = new ReentrantLock();

    private ArrayList<String[]> Musicas = new ArrayList<String[]>();

    String [][] Queue = {};

    public Player() {
        ActionListener buttonListenerPlayNow = e -> start();

        ActionListener buttonListenerRemove = e -> removeSong();

        ActionListener buttonListenerAddSong = e -> addSong();

        ActionListener buttonListenerPlayPause = e -> playPause();

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
                "Spotofi",
                this.Queue
        );

        playerWindow.start();

        try {
            BufferedReader csvReader = new BufferedReader(new FileReader("musicas.csv"));
            String row;
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(",");
                Musicas.add(data);
                if (Integer.parseInt(data[6]) >= this.idCounter) {
                    this.idCounter = Integer.parseInt(data[6])+1;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
        } catch (IOException f) {
            System.out.println(f);
        } finally {
            updateQueue();
        }

        this.start = Instant.now();
        while (true) {
            this.stop = Instant.now();
            Duration dt = Duration.between(this.start, this.stop);
            if (dt.getSeconds() >= 1) {
                this.start = Instant.now();
                this.lock.lock();
                if (this.isPlaying) {
                    if (this.currentTime >= Integer.parseInt(this.Musicas.get(this.currentId)[5])) {
                        this.isPlaying = false;
                        this.currentTime -= 1;
                    }
                    this.currentTime += 1;
                    this.stop = Instant.now();

                    this.playerWindow.updateMiniplayer(
                            this.isActive,
                            this.isPlaying,
                            this.isRepeat,
                            (int) this.currentTime,
                            Integer.parseInt(this.Musicas.get(this.currentId)[5]),
                            this.currentId,
                            this.Queue.length
                    );
                }
                this.lock.unlock();
            }
        }
    }

    public void start() {
        this.lock.lock();
        this.currentId = getIdxFromId(String.valueOf(this.playerWindow.getSelectedSongID()));
        this.currentTime = 0;
        this.playerWindow.updatePlayingSongInfo(this.Musicas.get(this.currentId)[0],
                this.Musicas.get(this.currentId)[1],
                this.Musicas.get(this.currentId)[2]);
        this.isPlaying = true;
        this.playerWindow.updateMiniplayer(
                this.isActive,
                this.isPlaying,
                this.isRepeat,
                (int) this.currentTime,
                Integer.parseInt(this.Musicas.get(this.currentId)[5]),
                this.currentId,
                this.Queue.length
                );
        this.playerWindow.enableScrubberArea();
        this.start = Instant.now();
        this.playerWindow.updatePlayPauseButton(this.isPlaying);
        this.lock.unlock();
    }

    public void playPause() {
        this.lock.lock();
        this.isPlaying = !this.isPlaying;
        this.start = Instant.now();
        this.playerWindow.updatePlayPauseButton(this.isPlaying);
        this.lock.unlock();
    }

    public void addSong() {
        ActionListener buttonListenerAddSongOK = a -> {
            this.lock.lock();
            String [] song = this.addSongWindow.getSong();
            this.Musicas.add(song);
            this.idCounter += 1;
            updateQueue();
            this.addSongWindow.interrupt();
            this.addSongWindow = null;
            try {
                saveMusicas();
            } catch (IOException e) {
                System.out.println(e);
            }
            this.lock.unlock();
        };
        this.lock.lock();
        this.addSongWindow = new AddSongWindow(String.valueOf(this.idCounter),
                buttonListenerAddSongOK,
                this.playerWindow.getAddSongWindowListener());

        addSongWindow.start();
        this.lock.unlock();
    }

    public void removeSong() {
        this.lock.lock();
        int removedIdx = getIdxFromId(String.valueOf(this.playerWindow.getSelectedSongID()));
        if (removedIdx == this.currentId){
            this.playerWindow.resetMiniPlayer();
            this.currentTime = 0;
            this.isPlaying = false;
        } else if (removedIdx < this.currentId) {
            this.currentId -= 1;
        }
        this.Musicas.remove(removedIdx);
        updateQueue();
        try {
            saveMusicas();
        } catch (IOException e) {
            System.out.println(e);
        }
        this.lock.unlock();
    }

    public void updateQueue() {
        this.Queue = this.Musicas.toArray(new String[this.Musicas.size()][7]);
        playerWindow.updateQueueList(this.Queue);
    }

    public int getIdxFromId(String Id) {
        int result = -1;
        for (int i = 0; i < this.Musicas.size(); i++) {
            if (this.Musicas.get(i)[6].equals(Id)) {
                result = i;
                break;
            }
        }
        return result;
    }

    public void saveMusicas() throws IOException{
        File file = new File("musicas.csv");
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
        for (int i = 0; i < this.Musicas.size(); i++) {
            String newLine = String.join(",", this.Musicas.get(i));
            bw.write(newLine);
            bw.newLine();
        }
        bw.close();
        fw.close();
    }
}