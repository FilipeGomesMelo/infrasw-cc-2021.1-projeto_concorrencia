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
    // declaração de variáveis e atributos iniciais
    private PlayerWindow playerWindow;
    private boolean isActive = true;
    private boolean isPlaying = false;
    private boolean isRepeat = false;
    private double currentTime = 0;
    private int currentId = -1;
    private int idCounter = 0;
    private Instant start;
    private Instant stop;

    private AddSongWindow addSongWindow = null;

    private final Lock lock = new ReentrantLock();

    // declaração da estrutura de dados utilizada para armazenar as músicas
    // adicionadas
    private ArrayList<String[]> Musicas = new ArrayList<String[]>();

    String[][] Queue = {};

    public Player() {
        // features implementadas até agora: Play, Pause, Adicionar música e Remover
        // música
        ActionListener buttonListenerPlayNow = e -> start();

        ActionListener buttonListenerRemove = e -> removeSong();

        ActionListener buttonListenerAddSong = e -> addSong();

        ActionListener buttonListenerPlayPause = e -> playPause();

        ActionListener buttonListenerStop = e -> {
            System.out.println("buttonListenerStop");
        };

        ActionListener buttonListenerNext = e -> playNext();

        ActionListener buttonListenerPrevious = e -> playPrevious();

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
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        };

        MouseMotionListener scrubberListenerMotion = new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                System.out.println("mouseDragged");
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
        };

        this.playerWindow = new PlayerWindow(buttonListenerPlayNow, buttonListenerRemove, buttonListenerAddSong,
                buttonListenerPlayPause, buttonListenerStop, buttonListenerNext, buttonListenerPrevious,
                buttonListenerShuffle, buttonListenerRepeat, scrubberListenerClick, scrubberListenerMotion, "Spotofi",
                this.Queue);

        try {
            BufferedReader csvReader = new BufferedReader(new FileReader("musicas.csv"));
            String row;
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(",");
                Musicas.add(data);
                if (Integer.parseInt(data[6]) >= this.idCounter) {
                    this.idCounter = Integer.parseInt(data[6]) + 1;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
        } catch (IOException f) {
            System.out.println(f);
        } finally {
            updateQueue();
        }

        // funcionamento geral do player
        playerWindow.start(); // inicia a thread
        this.start = Instant.now(); // marca o instante de início da música, do qual contaremos sua duração
        while (true) { // loop de reprodução da música
            this.stop = Instant.now();
            Duration dt = Duration.between(this.start, this.stop); // verificamos o intervalo entre o momento atual e o
                                                                   // último verificado
            if (dt.getSeconds() >= 1) { // a cada um segundo, a interface avança a reprodução
                this.start = Instant.now();
                this.lock.lock(); // damos lock para poder alterar valores de atributos do player
                if (this.isPlaying) { // caso a música esteja tocando, atualizamos o valor do tempo atual na interface
                    this.currentTime += 1;
                    this.stop = Instant.now();

                    this.playerWindow.updateMiniplayer( // atualização dos parâmetros
                            this.isActive, this.isPlaying, this.isRepeat, (int) this.currentTime,
                            Integer.parseInt(this.Musicas.get(this.currentId)[5]), this.currentId, this.Queue.length);

                    if (this.currentTime >= Integer.parseInt(this.Musicas.get(this.currentId)[5])) { // condição de
                        // término da
                        // música, scrubber
                        // volta pro início
                        if (!playNext()) {
                            this.isPlaying = false;
                            this.currentId = -1;
                            this.playerWindow.resetMiniPlayer(); // resetamos o miniplayer e os atributos do player
                            this.currentTime = 0;
                            this.isPlaying = false;
                        }
                    }
                }
            }
        }
    }

    public void start() { // configurações para começar a tocar uma música
        this.lock.lock(); // damos lock para poder alterar valores de atributos do player
        try {
            this.currentId = getIdxFromId(String.valueOf(this.playerWindow.getSelectedSongID()));
            this.currentTime = 0;
            this.playerWindow.updatePlayingSongInfo(this.Musicas.get(this.currentId)[0], // configuração da interface para
                    // mostrar a música tocada
                    this.Musicas.get(this.currentId)[1], this.Musicas.get(this.currentId)[2]);
            this.isPlaying = true;
            this.playerWindow.updateMiniplayer( // inicialização de parâmetros para indicar que a música está tocando
                    this.isActive, this.isPlaying, this.isRepeat, (int) this.currentTime,
                    Integer.parseInt(this.Musicas.get(this.currentId)[5]), this.currentId, this.Queue.length);
            this.playerWindow.enableScrubberArea();
            this.start = Instant.now();
            this.playerWindow.updatePlayPauseButton(this.isPlaying);
        } finally {
            this.lock.unlock(); // unlock após as alterações para liberar a zona crítica
        }
    }

    public void playPause() {
        this.lock.lock(); // damos lock para poder alterar valores de atributos do player
        try {
            this.isPlaying = !this.isPlaying; // sempre inverteremos o status da música de play para pause ou de pause para
            // play quando o botão for apertado
            this.start = Instant.now();
            this.playerWindow.updatePlayPauseButton(this.isPlaying); // atualizamos a interface
        } finally {
            this.lock.unlock(); // unlock após as alterações para liberar a zona crítica
        }
    }

    public void addSong() {
        ActionListener buttonListenerAddSongOK = a -> {
            this.lock.lock(); // damos lock para poder alterar valores de atributos do player
            try {
                String[] song = this.addSongWindow.getSong(); // pegamos as informações da música pela janela AddSong
                this.Musicas.add(song); // adicionamos na nossa estrutura
                this.idCounter += 1; // atualizamos o ID para que se mantenha sempre diferente para cada música
                updateQueue(); // atualizamos a fila de músicas
                this.addSongWindow.interrupt(); // finalizamos a thread de adicionar música, pois terminamos de usar suas
                // funcionalidades
                this.addSongWindow = null;
                try {
                    saveMusicas();
                } catch (IOException e) {
                    System.out.println(e);
                }
            } finally {
                this.lock.unlock(); // unlock após as alterações para liberar a zona crítica
            }
        };
        this.lock.lock(); // damos lock para poder alterar valores de atributos do player
        try {
            this.addSongWindow = new AddSongWindow(String.valueOf(this.idCounter), // Inicializando os parâmetros do AddSong
                    // Window
                    buttonListenerAddSongOK, this.playerWindow.getAddSongWindowListener());

            addSongWindow.start(); // Iniciando a thread do AddSong Window
        } finally {
            this.lock.unlock(); // unlock após as alterações para liberar a zona crítica
        }
    }

    public void removeSong() {
        this.lock.lock(); // damos lock para poder alterar valores de atributos do player
        try {
            int removedIdx = getIdxFromId(String.valueOf(this.playerWindow.getSelectedSongID())); // pegamos o index da
            // música que deve ser
            // removida da fila
            if (removedIdx == this.currentId) { // caso se deseje remover a música atual
                this.playerWindow.resetMiniPlayer(); // resetamos o miniplayer e os atributos do player
                this.currentTime = 0;
                this.isPlaying = false;
            } else if (removedIdx < this.currentId) { // caso seja outra música, devemos alterar o ID da atual, por conta da
                // posição na fila
                this.currentId -= 1;
            }
            this.Musicas.remove(removedIdx); // removemos a música da estrutura de dados
            updateQueue(); // atualizamos a fila mostrada no player
            try {
                saveMusicas();
            } catch (IOException e) {
                System.out.println(e);
            }
        } finally {
            this.lock.unlock(); // unlock após as alterações para liberar a zona crítica
        }
    }

    public void updateQueue() { // passamos as músicas da nossa estrutura para a fila sempre que atualizamos
                                // alguma informação
        this.Queue = this.Musicas.toArray(new String[this.Musicas.size()][7]);
        playerWindow.updateQueueList(this.Queue);
    }

    public int getIdxFromId(String Id) { // retorna o index da música na nossa estrutura para o dado ID
        int result = -1;
        for (int i = 0; i < this.Musicas.size(); i++) {
            if (this.Musicas.get(i)[6].equals(Id)) {
                result = i;
                break;
            }
        }
        return result;
    }

    public void saveMusicas() throws IOException {
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

    public boolean playNext() {
        lock.lock();
        if (this.currentId < this.Musicas.size()-1) {
            this.currentId += 1;
            this.currentTime = 0;
            this.start = Instant.now();

            this.playerWindow.updatePlayingSongInfo(this.Musicas.get(this.currentId)[0],
                    this.Musicas.get(this.currentId)[1], this.Musicas.get(this.currentId)[2]);

            this.playerWindow.updateMiniplayer(this.isActive, this.isPlaying, this.isRepeat,
                    (int) this.currentTime, Integer.parseInt(this.Musicas.get(this.currentId)[5]),
                    this.currentId, this.Queue.length);

            lock.unlock();
            return true;
        }
        lock.unlock();
        return false;
    }

    public void playPrevious() {
        lock.lock();
        if (this.currentId > 0){
            this.currentId -= 1;
            this.currentTime = 0;
            this.start = Instant.now();

            this.playerWindow.updatePlayingSongInfo(this.Musicas.get(this.currentId)[0],
                    this.Musicas.get(this.currentId)[1], this.Musicas.get(this.currentId)[2]);

            this.playerWindow.updateMiniplayer(this.isActive, this.isPlaying, this.isRepeat,
                    (int) this.currentTime, Integer.parseInt(this.Musicas.get(this.currentId)[5]),
                    this.currentId, this.Queue.length);
        }
        lock.unlock();
    }
}
