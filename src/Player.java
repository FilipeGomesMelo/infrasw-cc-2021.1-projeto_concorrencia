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

            }

            @Override
            public void mousePressed(MouseEvent e) {
                pressedMouse();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                releaseMouse();
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
                draggedMouse();
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
    }

    public void comecar() {
        this.start = Instant.now(); // marca o instante de início da música, do qual contaremos sua duração
        while (true) { // loop de reprodução da música
            this.stop = Instant.now();
            Duration dt = Duration.between(this.start, this.stop); // verificamos o intervalo entre o momento atual e o
            // último verificado
            if (dt.getSeconds() >= 1) { // a cada um segundo, a interface avança a reprodução
                try {
                    this.lock.lock(); // damos lock para poder alterar valores de atributos do player
                    this.start = Instant.now();
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
                } finally {
                    this.lock.unlock();
                }
            }
        }
    }

    public void start() { // configurações para começar a tocar uma música
        Thread t_playNow = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    lock.lock(); // damos lock para poder alterar valores de atributos do player
                    currentId = getIdxFromId(String.valueOf(playerWindow.getSelectedSongID()));
                    currentTime = 0;
                    playerWindow.updatePlayingSongInfo(Musicas.get(currentId)[0], // configuração da interface para
                            // mostrar a música tocada
                            Musicas.get(currentId)[1], Musicas.get(currentId)[2]);
                    isPlaying = true;
                    playerWindow.updateMiniplayer( // inicialização de parâmetros para indicar que a música está tocando
                            isActive, isPlaying, isRepeat, (int) currentTime,
                            Integer.parseInt(Musicas.get(currentId)[5]), currentId, Queue.length);
                    playerWindow.enableScrubberArea();
                    start = Instant.now();
                    playerWindow.updatePlayPauseButton(isPlaying);
                } finally {
                    lock.unlock(); // unlock após as alterações para liberar a zona crítica
                }
            }
        });

        t_playNow.start();
    }

    public void playPause() {
        Thread t_playPause = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    lock.lock(); // damos lock para poder alterar valores de atributos do player
                    isPlaying = !isPlaying; // sempre inverteremos o status da música de play para pause ou de pause para
                    // play quando o botão for apertado
                    start = Instant.now();
                    playerWindow.updatePlayPauseButton(isPlaying); // atualizamos a interface
                } finally {
                    lock.unlock(); // unlock após as alterações para liberar a zona crítica
                }
            }
        });

        t_playPause.start();
    }

    public void addSong() {
        Thread t_addSong = new Thread(new Runnable() {
            @Override
            public void run() {
                ActionListener buttonListenerAddSongOK = a -> {
                    Thread t_addSongOk = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                lock.lock(); // damos lock para poder alterar valores de atributos do player
                                String[] song = addSongWindow.getSong(); // pegamos as informações da música pela janela AddSong
                                Musicas.add(song); // adicionamos na nossa estrutura
                                idCounter += 1; // atualizamos o ID para que se mantenha sempre diferente para cada música
                                updateQueue(); // atualizamos a fila de músicas
                                addSongWindow.interrupt(); // finalizamos a thread de adicionar música, pois terminamos de usar suas
                                // funcionalidades
                                addSongWindow = null;
                                try {
                                    saveMusicas();
                                } catch (IOException e) {
                                    System.out.println(e);
                                }
                            } finally {
                                lock.unlock(); // unlock após as alterações para liberar a zona crítica
                            }
                        }
                    });

                    t_addSongOk.start();
                };

                try {
                    lock.lock(); // damos lock para poder alterar valores de atributos do player
                    addSongWindow = new AddSongWindow(String.valueOf(idCounter), // Inicializando os parâmetros do AddSong
                            // Window
                            buttonListenerAddSongOK, playerWindow.getAddSongWindowListener());

                    addSongWindow.start(); // Iniciando a thread do AddSong Window
                } finally {
                    lock.unlock(); // unlock após as alterações para liberar a zona crítica
                }
            }
        });

        t_addSong.start();
    }

    public void removeSong() {
        Thread t_removeSong = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    lock.lock(); // damos lock para poder alterar valores de atributos do player
                    int removedIdx = getIdxFromId(String.valueOf(playerWindow.getSelectedSongID())); // pegamos o index da
                    // música que deve ser
                    // removida da fila
                    if (removedIdx == currentId) { // caso se deseje remover a música atual
                        playerWindow.resetMiniPlayer(); // resetamos o miniplayer e os atributos do player
                        currentTime = 0;
                        isPlaying = false;
                    } else if (removedIdx < currentId) { // caso seja outra música, devemos alterar o ID da atual, por conta da
                        // posição na fila
                        currentId -= 1;
                    }
                    Musicas.remove(removedIdx); // removemos a música da estrutura de dados
                    updateQueue(); // atualizamos a fila mostrada no player
                    try {
                        saveMusicas();
                    } catch (IOException e) {
                        System.out.println(e);
                    }
                } finally {
                    lock.unlock(); // unlock após as alterações para liberar a zona crítica
                }
            }
        });

        t_removeSong.start();
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
        boolean deu_certo = false;
        try {
            lock.lock();
            if (this.currentId < this.Musicas.size() - 1) {
                this.currentId += 1;
                this.currentTime = 0;
                this.start = Instant.now();

                this.playerWindow.updatePlayingSongInfo(this.Musicas.get(this.currentId)[0],
                        this.Musicas.get(this.currentId)[1], this.Musicas.get(this.currentId)[2]);

                this.playerWindow.updateMiniplayer(this.isActive, this.isPlaying, this.isRepeat,
                        (int) this.currentTime, Integer.parseInt(this.Musicas.get(this.currentId)[5]),
                        this.currentId, this.Queue.length);

                deu_certo = true;
            }
        } finally {
            lock.unlock();
            return deu_certo;
        }
    }

    public void playPrevious() {
        Thread t_playPrevious = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    lock.lock();
                    if (currentId > 0) {
                        currentId -= 1;
                        currentTime = 0;
                        start = Instant.now();

                        playerWindow.updatePlayingSongInfo(Musicas.get(currentId)[0],
                                Musicas.get(currentId)[1], Musicas.get(currentId)[2]);

                        playerWindow.updateMiniplayer(isActive, isPlaying, isRepeat,
                                (int) currentTime, Integer.parseInt(Musicas.get(currentId)[5]),
                                currentId, Queue.length);
                    }
                } finally {
                    lock.unlock();
                }
            }
        });

        t_playPrevious.start();
    }

    public void pressedMouse() {
        System.out.println("mousePressed");
        Thread t_pressedMouse= new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    lock.lock();
                    isPlaying = false;
                    currentTime = playerWindow.getScrubberValue();
                    playerWindow.updateMiniplayer( // atualização dos parâmetros
                            isActive, isPlaying, isRepeat, (int) currentTime,
                            Integer.parseInt(Musicas.get(currentId)[5]), currentId, Queue.length);
                } catch (java.lang.IndexOutOfBoundsException e) {
                    // Quando o scrubber está desativado e a pessoa tenta mexer, nada acontece
                    currentTime = 0;
                } finally {
                    lock.unlock();
                }
            }
        });

        t_pressedMouse.start();
    }

    public void releaseMouse() {
        System.out.println("mouseReleased");
        Thread t_releaseMouse = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    lock.lock();
                    currentTime = playerWindow.getScrubberValue();
                    isPlaying = true;
                    start = Instant.now();
                    playerWindow.updateMiniplayer( // atualização dos parâmetros
                            isActive, isPlaying, isRepeat, (int) currentTime,
                            Integer.parseInt(Musicas.get(currentId)[5]), currentId, Queue.length);
                } catch (java.lang.IndexOutOfBoundsException e) {
                    // Quando o scrubber está desativado e a pessoa tenta mexer, nada acontece
                    currentTime = 0;
                    isPlaying = false;
                } finally {
                    lock.unlock();
                }
            }
        });

        t_releaseMouse.start();
    }

    public void draggedMouse() {
        System.out.println("mouseDragged");
        Thread t_draggedMouse = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    lock.lock();
                    currentTime = playerWindow.getScrubberValue();
                    playerWindow.updateMiniplayer( // atualização dos parâmetros
                            isActive, isPlaying, isRepeat, (int) currentTime,
                            Integer.parseInt(Musicas.get(currentId)[5]), currentId, Queue.length);
                } catch (java.lang.IndexOutOfBoundsException e) {
                    // Quando o scrubber está desativado e a pessoa tenta mexer, nada acontece
                    currentTime = 0;
                }
                finally {
                    lock.unlock();
                }
            }
        });

        t_draggedMouse.start();
    }
}
