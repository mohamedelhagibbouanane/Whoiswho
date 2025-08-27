
import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author Mohamed El Hagib Bouanane
 */
/**
 * Clase que contiene métodos utilitarios relacionados con personajes.
 */

public class AuxMethods {

    /**
     * Genera aleatoriamente un personaje seleccionando uno de los subarreglos
     * de una lista bidimensional.
     *
     * @param charactersList Arreglo bidimensional que contiene diferentes
     * listas de personajes. Cada subarreglo representa un personaje con sus
     * atributos.
     * @return Un arreglo unidimensional que representa un personaje
     * seleccionado aleatoriamente.
     */
    private Clip clip;

    public static String[] generateCharcter(String[][] charactersList) {
        // Crear una instancia de Random para generar números aleatorios
        Random random = new Random();

        // Seleccionar un índice aleatorio de la lista de personajes
        int randomArray = random.nextInt(0, charactersList.length);

        // Obtener la longitud del subarreglo seleccionado (no es estrictamente necesario aquí)
        int arraysLength = charactersList[randomArray].length;

        // Crear un nuevo arreglo para almacenar el personaje seleccionado (aunque se sobrescribe después)
        String[] selectedCharacter = new String[arraysLength];

        // Asignar directamente el subarreglo seleccionado al resultado
        selectedCharacter = charactersList[randomArray];

        return selectedCharacter;
    }

    /**
     * Muestra un cuadro de diálogo al usuario para confirmar si desea iniciar
     * una nueva partida. Si el usuario acepta, se cierra la ventana actual y se
     * inicia una nueva instancia del juego. Si el usuario rechaza, se muestra
     * un mensaje de despedida y se cierra la aplicación.
     *
     * @param parent Componente padre del cuadro de diálogo, normalmente la
     * ventana actual (por ejemplo, un JFrame).
     * @param name Nombre del jugador o dato necesario para iniciar el nuevo
     * juego.
     */
    public static void newGameOption(Component parent, String name) {

        // Muestra un cuadro de confirmación con las opciones "Sí" y "No"
        int systemAnswer = JOptionPane.showConfirmDialog(
                parent,
                "¿Desea iniciar una nueva partida?",
                "Continuar",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        // Si el usuario elige "Sí", se cierra la ventana actual y se inicia una nueva partida
        if (systemAnswer == JOptionPane.YES_OPTION) {

            // Cierra la ventana actual si es un JFrame
            ((JFrame) parent).dispose();

            // Lanza una nueva instancia del juego de forma segura en el hilo de eventos de Swing
            SwingUtilities.invokeLater(() -> new GameScreen(name).setVisible(true));
        } else {
            // Si el usuario elige "No", muestra un mensaje de despedida y termina la aplicación
            JOptionPane.showMessageDialog(
                    parent,
                    "Gracias por utilizar mi juego",
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE
            );

            int finalScore = ((GameScreen) parent).getScore();
            System.out.println("score" + finalScore);
//            showScore(finalScore, name);
            try {
                generatScoreFile(parent, finalScore, ((GameScreen) parent).getPlayerName());
            } catch (IOException ex) {
                System.getLogger(GameScreen.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
            System.exit(0);
        }
    }

    /**
     * Maneja la lógica del botón de adivinanza del personaje. Comprueba si la
     * entrada del usuario coincide con el personaje secreto y responde en
     * consecuencia.
     *
     * @param parent Componente padre, normalmente la ventana principal del
     * juego.
     * @param jTextField1 Campo de texto donde el usuario escribe su adivinanza.
     * @param name Nombre del jugador (utilizado para reiniciar el juego si
     * acierta).
     */
    public static void guessButton(Component parent, JTextField jTextField1, String name) {
        // Obtener el texto ingresado por el usuario
        String userCharacter = jTextField1.getText().trim();  // Se recomienda usar trim() para evitar espacios vacíos

        // Imprimir por consola para depuración
        System.out.println("Adivina: " + userCharacter);

        // Obtener el personaje que debe adivinarse (índice 0 suele ser el nombre)
        String[] characterArray = ((GameScreen) parent).getSelectedCharacter();

        // Validar si el campo está vacío
        if (userCharacter.isEmpty()) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Este campo no puede estar vacío.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        } // Verificar si la adivinanza es correcta (sin importar mayúsculas/minúsculas)
        else if (userCharacter.equalsIgnoreCase(characterArray[0])) {
            JOptionPane.showMessageDialog(
                    parent,
                    "¡Eres una máquina! Sí, soy " + characterArray[0]
            );

            // Preguntar si desea iniciar una nueva partida
            newGameOption(parent, name);

            // Cerrar ventana actual (en caso no se haya cerrado en newGameOption)
            if (parent instanceof JFrame jFrame) {
                jFrame.dispose();
            }
        } // Si la adivinanza es incorrecta
        else {
            JOptionPane.showMessageDialog(
                    parent,
                    "¡Que va! No soy " + userCharacter.toLowerCase() + ", piensa un poco más..."
            );
        }

        // Limpiar el campo de texto para la siguiente adivinanza
        jTextField1.setText("");
    }

    /**
     * Verifica si la adivinanza ingresada por el usuario es correcta. A
     * diferencia de {@code guessButton}, este método parece usarse para
     * intentos sin opción de repetir.
     *
     * @param parent Componente padre (usualmente una ventana), usado para los
     * cuadros de diálogo.
     * @param jTextField1 Campo de texto donde el usuario introduce su
     * adivinanza.
     */
    public static void requiredGuess(Component parent, JTextField jTextField1) {
        // Obtener y limpiar el texto ingresado por el usuario
        String userCharacter = jTextField1.getText().trim();

        // Imprimir la entrada del usuario (para depuración)
        System.out.println("Adivina: " + userCharacter);

        // Obtener el personaje correcto (índice 0 se asume como el nombre del personaje)
        String[] characterArray = ((GameScreen) parent).getSelectedCharacter();

        // Validar si el campo está vacío
        if (userCharacter.isEmpty()) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Este campo no puede estar vacío",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        } // Si la adivinanza es correcta
        else if (userCharacter.equalsIgnoreCase(characterArray[0])) {
            JOptionPane.showMessageDialog(
                    parent,
                    "¡Eres una máquina! Sí soy " + userCharacter.toLowerCase()
            );
        } // Si es incorrecta
        else {
            JOptionPane.showMessageDialog(
                    parent,
                    "Game Over. Wili go to Wisconsin."
            );
        }

        // Limpiar el campo de texto
        jTextField1.setText("");
    }

    public void playSound(String soundPath) {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(getClass().getResource(soundPath));
            this.clip = AudioSystem.getClip(); // Usa this.clip
            this.clip.open(audioIn);
            this.clip.start(); //play loop
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
        }
    }

    public void stopAudio() {
        if (clip != null && clip.isRunning()) {
            this.clip.stop();
            this.clip.close();
        }
    }


public static void generatScoreFile(Component parent, int score, String playerName) throws IOException {

    String directoryPath = "Scores";
    String fileName = "scores.txt";
    Path scoreFilePath = Paths.get(directoryPath, fileName);

    // Crear la carpeta si no existe
    File directory = new File(directoryPath);
    if (!directory.exists()) {
        directory.mkdir();
    }

    // Lista para guardar pares (nombre, score)
    List<String[]> scoreEntries = new ArrayList<>();

    // Leer el archivo si ya existe
    if (Files.exists(scoreFilePath)) {
        List<String> lines = Files.readAllLines(scoreFilePath);
        for (String line : lines) {
            String[] parts = line.split("       ----------->", 2);
            if (parts.length == 2) {
                try {
                    String name = parts[0].replaceAll("^\\d+\\.\\s*", ""); // Eliminar numeración previa
                    int existingScore = Integer.parseInt(parts[1]);
                    scoreEntries.add(new String[]{name, String.valueOf(existingScore)});
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    // Limpiar numeración si viene con el nombre del jugador
    playerName = playerName.replaceAll("^\\d+\\.\\s*", "");

    // Agregar el nuevo score
    scoreEntries.add(new String[]{playerName, String.valueOf(score)});

    // Ordenar de mayor a menor por score
    scoreEntries.sort((a, b) -> Integer.parseInt(b[1]) - Integer.parseInt(a[1]));

    // Limitar a las 3 mejores puntuaciones
    if (scoreEntries.size() > 3) {
        scoreEntries = scoreEntries.subList(0, 3);
    }

    // Escribir nuevamente el archivo con las mejores puntuaciones
    int i = 1;
    try (PrintWriter pw = new PrintWriter(scoreFilePath.toFile())) {
        for (String[] entry : scoreEntries) {
            pw.println(i + ". " + entry[0] + "       ----------->" + entry[1]);
            i++;
        }
    }
}



}
