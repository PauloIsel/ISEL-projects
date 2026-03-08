package trabs.trab3.minesweeper;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import java.util.function.BiConsumer;

import trabs.trab3.minesweeper.gui.*;
import trabs.trab3.minesweeper.players.Leaderboard;
import trabs.trab3.minesweeper.players.PlayerStats;

import static trabs.trab3.minesweeper.gui.Utils.*;

/**
 * <p> Implementa um visualizador em modo grafico.</p>
 * <p> O tabuleiro do jogo é implementado com um JPanel contendo
 *     componentes graficas do tipo Square. O layout é um GridLayout
 *     com o numero de linhas e de colunas do objecto Game ao qual é
 *     associado na construcao, e no inicio de cada jogo.
 * </p>
 * <p> Quando  é informado que algo mudou no jogo (vazia, mina,
 *     adjacent a mina, ou bandeira) altera o caracter da matriz de
 *     acordo com o tipo de pedido e o aspecto visual da quadricula.
 * </p>
 * <p> Quando é informado do inicio do jogo, caso a dimensao do tabuleiro
 *     seja alterada cria um novo tabuleiro.
 * </p>
 * <p> Quando o jogo termina quer seja porque o jogador tenha ganho, ou
 *     porque tenha perdido informa o jogador do fim do jogo.</p>
 *
 * <p> Alem de ser um visualizador, com esta interface grafica tambem é
 *     possivel desencadear as operações de descobrir quadriculas, colocar e
 *     retirar bandeiras, ou iniciar o jogo.
 * </p>
 * <p> Fazendo um click com o botão esquerdo do mouse sobre uma quadricula,
 *     é possivel enviar a ordem ao jogo para descobrir a quadricula. </p>
 * <p> Fazendo um click com o botão direito do mouse sobre uma quadricula,
 *     é possivel enviar a ordem ao jogo para virar a bandeira. </p>
 * <p> Usando as opções do menu bar é possivel enviar a ordem ao jogo de
 *     inicio de jogo, com ou sem mudança de dimensões.
 *
 */
public class MinesweeperFrame extends JFrame implements GameListener {
	public static final Border INSIDE_BORDER =
			new CompoundBorder( new BevelBorder(BevelBorder.LOWERED),
					new EmptyBorder(2,2,2,2)
			);
	public static final Border OUTSIDE_BORDER = new CompoundBorder(
			new BevelBorder(BevelBorder.RAISED),
			new CompoundBorder( new EmptyBorder(1,1,1,1),
					new BevelBorder(BevelBorder.LOWERED)));
	private static final ImageIcon SMILE_ICON = getImageIcon("smile.jpg", "smile");
	private static final ImageIcon MOAN_ICON = getImageIcon("moan.jpg", "moan");
	private BiConsumer<Integer, Integer> showAction(String filename, String text){
		ImageIcon img = getImageIcon(filename, text);
		return img == null
				? (l, c)-> getSquare(l, c).show(text)
				: (l, c)-> getSquare(l, c).show(img);
	}
	private final BiConsumer<Integer, Integer> showMine = showAction("mine.jpg", "M");
	private final BiConsumer<Integer, Integer> showBanner = showAction("banner.jpg", "B");

	private JComponent board;
	private Game game;
	private Display displayTime, displayBanners;

	private long initialTime = System.currentTimeMillis();
	private Timer timer;

	private final Leaderboard leaderboard = new Leaderboard();
	private String currentDifficulty = "beginner";
	private final PlayerStats playerStats = new PlayerStats("Player");

	private void startDefaultGame() {
		game.start(game.getNumberOfLines(), game.getNumberOfColumns(), game.getNumberOfMines());
	}

	private void startGame(String difficulty) {
		switch (difficulty) {
			case "beginner":
				currentDifficulty = "beginner";
				game.start(9, 9, 10);
				break;
			case "intermediate":
				currentDifficulty = "intermediate";
				game.start(16, 16, 40);
				break;
			case "advanced":
				currentDifficulty = "advanced";
				game.start(16, 30, 99);
				break;
		}
	}

	private void setPlayerName() {
		String name = JOptionPane.showInputDialog(this, "Enter your name", playerStats.getName());
		if (name != null && name.isEmpty()) name = null;
		playerStats.setName(name);
	}

	public MinesweeperFrame(Game g ){
		super("Minesweeper");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		game = g;

		Container cp = getContentPane();
		((JComponent)cp).setBorder(OUTSIDE_BORDER);
		cp.add( board = makeBoardComponent(), BorderLayout.CENTER);

		game.addGameListener(this );

		// Acrescentar o component de informaçăo com: o número de bandeiras;
		// o botăo de start; e o tempo de jogo em segundos.
		cp.add( makeInfoComponent(), BorderLayout.NORTH);

		// Acrescentar o menu
		JMenuBar menuBar = new JMenuBar();
		menuBar.add( makeMenuOption( game ));
		setJMenuBar( menuBar );

		setPlayerName();

		this.setResizable(false);
		pack();
	}

	/**
	 * Cria o Panel superior. No lado superior esquerdo o número
	 * de  bandeiras que  estão marcadas, no  meio um  botão que
	 * permite o  início do  jogo, e no  lado  superior esquerdo
	 * o número de minutos de jogo.
	 */
	private JComponent makeInfoComponent(  ) {
		JPanel infoPanel = new JPanel();

		infoPanel.setLayout( new BorderLayout() );
		displayTime = new Display( 3, 0 );
		infoPanel.add( displayTime, BorderLayout.EAST );

		JButton startButton = new JButton( SMILE_ICON );
		startButton.addActionListener( e-> startDefaultGame() );
		infoPanel.add( startButton, BorderLayout.CENTER );

		displayBanners = new Display( 3, 0 );
		infoPanel.add( displayBanners, BorderLayout.WEST );

		return infoPanel;
	}

	/**
	 * Cria um menu com os itens de iniciaçăo de um novo jogo
	 * (iniciar o jogo corrente, e as tręs diferentes
	 * dimensőes de tabuleiro):
	 *  - "beginner" 9 x 9, 10 mines
	 *  - "intermediate" 16 x 16, 40 mines
	 *  - "advanced" 16 x 30, 99 mines
	 * @param game jogo
	 * @return A referęncia para o JMenu
	 */
	protected JMenu makeMenuOption(Game game) {
		JMenu optionsMenu = new JMenu( "options" );

		optionsMenu.add( Utils.makeJMenuItem("play", e-> startDefaultGame()));

		optionsMenu.add( Utils.makeJMenuItem("beginner", e-> startGame("beginner")));
		optionsMenu.add( Utils.makeJMenuItem("intermediate", e-> startGame("intermediate")));
		optionsMenu.add( Utils.makeJMenuItem("advanced", e-> startGame("advanced")));

		optionsMenu.add( Utils.makeJMenuItem("change name", e-> setPlayerName()));

		return optionsMenu;
	}

	/**
	 * Cria o componente quadricula na linha coluna especificada e
	 * adiciona o mouse listener para que quando for premido
	 * o botão esquerdo do mouse seja descoberta uma quadricula,
	 * quando premido o botão direiro coloca ou retira a bandeira.
	 */
	private Square makeSquare(int l, int c) {
		Square square = new Square( l, c );
		square.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent em) {
				if (timer == null) startDefaultGame();

				if (em.getButton() == MouseEvent.BUTTON1 )
					game.uncover( l, c );
				else
					game.turnBanner(l, c );
			}
		});
		return square;
	}
	/**
	 * <p>Cria o componente gráfico (tabuleiro) para
	 * visualização das quadriculas e os respectivos componentes gráficos
	 * que representam cada quadricula. </p>
	 * <p>Adiciona a cada quadricula um mouse listener. Quando for premido
	 * o botão esquerdo do mouse seja descoberta uma quadricula, quando
	 * premido o botão direiro coloca ou retira a bandeira.</p>
	 */
	protected JComponent makeBoardComponent(  ) {
		int numberOfLines = game.getNumberOfLines();
		int numberOfColumns = game.getNumberOfColumns();
		// Criar o componente tabuleiro.
		JPanel board = new JPanel( new GridLayout( numberOfLines, numberOfColumns) );
		board.setBorder(INSIDE_BORDER);
		for (int l = 0; l < numberOfLines; ++l)
			for ( int c= 0; c < numberOfColumns; ++c)
				// Adicionar a quadricula ao tabuleiro
				board.add( makeSquare( l, c ) );
		return board;
	}

	@Override
	public void gameStart( Game game ) {
		// Iniciar o timer
		initialTime = System.currentTimeMillis();

		if (timer != null)
			timer.stop();

		displayTime.setValue(0);
		timer = new Timer(1000, e-> {
			long time = (System.currentTimeMillis() - initialTime) / 1000;
			displayTime.setValue( (int)time );
		});

		timer.start();
	}

	@Override
	public void dimensionsChanged(Game game) {
		// Refazer o tabuleiro com as novas dimensoes
		Container cp = getContentPane();
		cp.remove(board);
		cp.add( board = makeBoardComponent(), BorderLayout.CENTER);
		pack();
	}

	/**
	 * Obter o componente quadricula que se encontra no na linha e na coluna
	 * especificada.
	 * @param line Linha.
	 * @param column Coluna
	 * @return A referencia para o componente gráfico que representa
	 *         a quadricula.
	 */
	private Square getSquare(int line, int column) {
		return (Square)board.getComponent( line*game.getNumberOfColumns() + column );
	}

	@Override
	public void cellCovered(int line, int column) {
		getSquare(line, column).hide();
	}

	@Override
	public void cellUncover(int line, int column) {
		getSquare(line, column).show("");
	}

	@Override
	public void cellUncover(int line, int column, int value) {
		getSquare(line,column).show( Integer.toString(value) );
	}

	@Override
	public void mineUncover(int line, int column) {
		showMine.accept( line, column);
	}

	@Override
	public void bannerPlaced(int line, int column) {
		showBanner.accept( line, column);
	}

	@Override
	public void numberOfBannersChanged(int number) {
		if (displayBanners == null) return;
		displayBanners.setValue( number );
	}

	private String formatTime(int timeInSeconds) {
		if (timeInSeconds == -1) return "0:00";

		int minutes = timeInSeconds / 60;
		int seconds = timeInSeconds % 60;
		return String.format("%d:%02d", minutes, seconds);
	}

	private String showStatsLeaderboard() {
		StringBuilder message = new StringBuilder(playerStats.toString());
		message.append("Best time: ").append(formatTime(leaderboard.getPlayerTime(playerStats.getName(), currentDifficulty))).append("\n\n");

		message.append("Leaderboard\n\n");

		if (leaderboard.getTop10(currentDifficulty).isEmpty()) {
			message.append("No entries yet.");
		} else {
			for (var entry : leaderboard.getTop10(currentDifficulty).entrySet()) {
				message.append(entry.getKey()).append(" - ").append(formatTime(entry.getValue())).append("\n");
			}
		}

		return message.toString();
	}

	@Override
	public void playerWin() {
		timer.stop();

		if (playerStats.getName() != null) {
			int finishTime = (int)((System.currentTimeMillis() - initialTime) / 1000);
			leaderboard.addTime(currentDifficulty, playerStats.getName(), finishTime);
		}

		playerStats.incrementGamesWon();

		ImageIcon resizedIcon = new ImageIcon(SMILE_ICON.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
		JOptionPane.showMessageDialog(this, "You win\n" + showStatsLeaderboard(), "Game Over", JOptionPane.INFORMATION_MESSAGE, resizedIcon);
	}

	@Override
	public void playerLose() {
		timer.stop();
		playerStats.incrementGamesLost();
		ImageIcon resizedIcon = new ImageIcon(MOAN_ICON.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
		JOptionPane.showMessageDialog(this, "You lose\n" + showStatsLeaderboard(), "Game Over", JOptionPane.INFORMATION_MESSAGE, resizedIcon);
	}
}
