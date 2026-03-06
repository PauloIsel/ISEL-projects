package trabs.trab3.minesweeper;

import java.util.ArrayList;
import java.util.Random;

import trabs.trab3.minesweeper.cells.*;

/**
 * Classe que implementa a lógica do jogo. Para a implementaçăo do
 * tabuleiro de jogo (board) usa uma matriz de (N+2)x(M+2), sendo N e M 
 * o número de linhas e colunas respetivamente.
 * 
 * Na matriz săo colocadas inicialmente células vazias, minas e células
 * que contęm o número de minas adjacentes, todas elas tapadas. Durante
 * o jogo săo destapadas células (uncover) e săo colocadas e retiradas
 * bandeiras (turnBanner).
 * 
 * Nesta implementaçăo inicialmente o tabuleiro é preenchido com células
 * vazias e posteriormente săo colocadas as minas em posiçőes aleatórias.
 * Quando se coloca aleatóriamente uma mina no tabuleiro as células 
 * que estăo em seu redor tęm que passar a número caso estejam vazias 
 * ou incrementar o número caso já sejam número. A açăo de passar a número
 * ou de incrementar é implementada na célula quando é informada que tem
 * a mina adjacente (adjacentMine).
 * 
 * Quando uma célula vazia é destapada tem que destapar as
 * células adjacentes que năo contenham minas de forma recorrente.
 *
 * 
 * As células que se encontram nos limites do tabuleiro podem năo ter
 * células adjacentes em cima, em baixo, ŕ esquerda ou ŕ direita. Para 
 * que os algoritmos năo tenham casos particulares adicionasse ŕ 
 * matriz duas linhas (a -1 e a N) e a cada linha duas colunas (-1 e M).
 * O métodos getCell e setCell virtualizam estas linhas.
 *
 * 
 */
public class GameMinesweeper implements Game, Board {
	private int numberOfLines, numberOfColumns, numberOfMines;
	private int numberOfBanners, numberOfHidden;
	private boolean gameOver = false;
	private int[] positionMines;
	protected ArrayList<GameListener> listeners = new ArrayList<>();
	protected Cell[][] board; // tabuleiro do jogo

	/**
	 * Instâncias de uma célula Empty, Mine e Border. Como estas células
	 * năo têm estado todas as minas do tabuleiro săo representadas pela
	 * mesma instância.
	 */
	protected static Cell BORDER_CELL = new DummyCell();
	protected static Cell EMPTY_CELL = new Empty();
	protected static Cell MINE_CELL = new Mine();

	public GameMinesweeper( int numberOfLines, int numberOfColumns, 
			                int numberOfMines ) {
		start( numberOfLines,  numberOfColumns,  numberOfMines);
	}

	/**
	 * Constrói a matriz de (N+2)x(M+2), sendo N e M o número de linhas
	 * e colunas respetivamente. Coloca no tabuleiro as minas e actualiza
	 * as células adjacentes ás minas.
	 * @param numberOfLines Número de linhas.
	 * @param numberOfColumns Número de colunas.
	 * @param numberOfMines Número de minas.
	 */
	@Override
	public void start(int numberOfLines, int numberOfColumns, int numberOfMines) {
		this.gameOver = false;
		this.numberOfBanners = 0;
		if ( numberOfMines != this.numberOfMines ) {
			this.numberOfLines = numberOfLines;
			this.numberOfColumns = numberOfColumns;
			this.numberOfMines = numberOfMines;
			board = new Cell[numberOfLines + 2][numberOfColumns + 2];
			listeners.forEach( (l) -> l.dimensionsChanged( this ));
			makeBorder();

			positionMines = new int[numberOfLines * numberOfColumns];
			for (int i = 0; i < positionMines.length; i++) {
				positionMines[i] = i;
			}
		}
		this.numberOfHidden = numberOfColumns * numberOfLines;
		makeBoard();
		putMines();

		listeners.forEach( (l) -> {
			l.gameStart( this );
			l.numberOfBannersChanged( 0 );
		});
	}

	/**
	 * Construir a cercadura.
	 * O tabuleiro de jogo ocupa as colunas da 0 ŕ numberOfColumns-1,
	 * das linhas 0 ŕ numberOfLines-1
	 * A cercadura ocupa:
	 *    A linha -1 e a linha numberOfLines
	 *    A coluna -1 e a coluna numberOfColumns das linhas
	 *    0 ŕ numberOfLines-1
	 */
	private void makeBorder() {
		for ( int c = -1; c <=numberOfColumns; ++c) {
			setCell( -1, c, BORDER_CELL);
			setCell( numberOfLines, c, BORDER_CELL);
		}
		for ( int l = 0; l <numberOfLines; ++l) {
			setCell( l, -1, BORDER_CELL);
			setCell( l, numberOfColumns, BORDER_CELL);
		}
	}

	/**
	 * Colocar células vazias no tabuleiro.
	 */
	private void makeBoard() {
		for (int l = 0; l < numberOfLines; ++ l)
			for ( int c = 0; c < numberOfColumns; ++ c)
				coveredCell( l, c, EMPTY_CELL);
	}

	/**
	 * Colocar as minas e os números no tabuleiro de jogo.
	 * Nos últimos numberOfMines indices do array indexMines ficam
	 * as posiçőes onde foram colocadas as minas.
	 */
	private void putMines() {
		Random r = new Random();

		for (int i = 0; i < numberOfMines; i++) {
			int j = r.nextInt(numberOfLines * numberOfColumns - i) + i;
			int aux = positionMines[i];
			positionMines[i] = positionMines[j];
			positionMines[j] = aux;

			int line = positionMines[i] / numberOfColumns;
			int column = positionMines[i] % numberOfColumns;
			setMine(line, column);
		}
	}

	/**
	 * Executa as acçőes que correspondem a destapar uma quadrícula.
	 *  A linha da quadrícula a destapar varia entre 0 e
	 *     getNumberOfLines()-1, a coluna varia entre 0 e 
	 *     getNumberOfColumns()-1.
	 * Quando o jogador destapa uma quadricula, caso esta seja:
	 *    - Mina, visualiza as minas e termina o jogo.
	 *    - Quadrícula vazia, é destapada a quadrícula e todas as 
	 *      quadrículas adjacentes que năo contenham minas de forma 
	 *      recorrente (expande a acao pelos adjacentes).
	 *    - Número, só é descoberto o próprio número.
	 *  Caso tenham sido destapadas quadrículas é informado o
	 * visualizador do numero de quadriculas que faltam destapar.
	 * Caso tenham sido destapadas todas as quadricúlas é assinalado
	 * que o jogo terminou e informado o visualizador.
	 * 
	 * @param line     Linha da quadrícula que se quer destapar.
	 * @param column   Coluna da quadrícula que se quer destapar.
	 * @return         true se destapou a quadrícula, false caso contrário.
	 *
	 */
	@Override
	public boolean uncover(int line, int column) {
		if ( gameOver ) return false;

		int numberOfCellUncover = getCell(line, column).uncover(this, line, column);
		if ( numberOfCellUncover != 0 ) {
			numberOfHidden-=numberOfCellUncover;
			if ( numberOfHidden == numberOfMines ) {
				gameOver = true;
				listeners.forEach( v -> v.playerWin());
			}
			return true;
		}
		return false;
	}

	/**
	 * Marca e desmarca com a bandeira (vira) uma quadrícula.
	 *  A linha da quadrícula varia entre 0 e getNumberOfLines()-1,
	 *     a coluna varia entre 0 e getNumberOfColumns()-1.
	 *  O visualizador é informado de quantas bandeiras estăo marcadas.
	 * @param line    Linha da quadrícula que se quer virar.
	 * @param column  Coluna da quadrícula que se quer virar.
	 * @return       true se foi colocada a bandeira.
	 * throws GameOverException    Se o jogo já terminou.
	 */
	@Override
	public boolean turnBanner(int line, int column) {
		if ( gameOver ) return false;

		int res = getCell(line, column).turnBanner(this, line, column);
        if ( res != 0 ) {
			numberOfBanners += res;
			listeners.forEach((v) -> v.numberOfBannersChanged(numberOfBanners));
			return true;
		}
		return false;
	}

	@Override
	public void addGameListener(GameListener gl) {
		this.listeners.add(gl) ;
		for( int l= 0; l < numberOfLines; ++l)
			for ( int c= 0; c < numberOfColumns; ++ c)
				getCell(l, c).notifyEvent(gl, l, c );
		gl.numberOfBannersChanged( numberOfBanners );
	}

	@Override
	public int getNumberOfLines() {
		return numberOfLines;
	}

	@Override
	public int getNumberOfColumns() {
		return numberOfColumns;
	}

	@Override
	public int getNumberOfMines() {
		return numberOfMines;
	}

	@Override
	public int getNumberOfBanners() {
		return numberOfBanners;
	}

	@Override
	public int getNumberOfHidden() {
		return numberOfHidden;
	}

	/**
	 * Retorna a célula que se encontra na posiçăo
	 * (linha e coluna) especificada do tabuleiro .
	 * Virtualiza uma matriz contendo a linha -1 e cada linha 
	 * a coluna -1.
	 * A linha varia entre -1 e getNumberOfLines(), a coluna varia
	 * entre -1 e getNumberOfColumns().
	 * @param line Linha
	 * @param column Coluna
	 * @return A célula que se encontra na posiçăo especificada.
	 */
	@Override
	public Cell getCell(int line, int column) {
		return board[line+1][column+1];
	}

	/**
	 * Coloca determinada célula no tabuleiro na posiçăo (linha e coluna)
	 * especificada. Virtualiza uma matriz contendo a linha -1 e cada 
	 * linha a coluna -1.
	 *  A linha varia entre -1 e getNumberOfLines(), a coluna varia
	 *     entre -1 e getNumberOfColumns().
	 * Chama o método report da célula de forma a que seja visualizada
	 * a alteraçăo que se operou no tabuleiro.
	 * @param line Linha
	 * @param column Coluna
	 * @param cell Célula a colocar.
	 */
	@Override
	public void setCell(int line, int column, Cell cell) {
		board[line+1][column+1] = cell;
		listeners.forEach( (l) -> cell.notifyEvent(l, line, column));
	}

	/**
	 * Coloca a instância de Mine coberta na posicăo (linha e coluna)
	 * especificada. Informa as células adjacentes que existe uma mina
	 * @param line Linha
	 * @param column Coluna
	 * @return A instância da célula Mine.
	 */
	private void setMine(int line, int column) {
		coveredCell( line, column, MINE_CELL );
		for ( int l = line-1; l <= line+1; ++ l)
			for ( int c = column-1; c <= column+1; ++ c)
				getCell(l, c).adjacentMine(this, l, c);
	}

	/**
	 * Instancia uma célula CoveredCell e coloca-a na posicăo (linha e coluna)
	 * especificada.
	 * @param line Linha
	 * @param column Coluna
	 * @param cell A célula que estava tapada.
	 */
	@Override
	public void coveredCell(int line, int column, Cell cell) {
		setCell( line, column, new CoveredCell( cell ) );
	}

	/**
	 * Instancia uma célula UncoveredCell e coloca-a na posicăo (linha e coluna)
	 * especificada.
	 * @param line Linha
	 * @param column Coluna
	 * @param uncovered A célula que estava tapada.
	 */
	@Override
	public void uncoveredCell(int line, int column, Cell uncovered) {
		setCell( line, column, new UncoveredCell( uncovered ));
	}

	/**
	 *Termina o jogo porque foi destapada a mina na posicăo
	 * (linha e coluna) especificada.
	 * Visualiza todas as minas, e informa o jogador que perdeu.
	 * @param line Linha
	 * @param column Coluna
	 * @param mine A mina que foi destapada.
	 */
	@Override
	public void uncoveredMine(int line, int column, Cell mine) {
		setCell( line, column, new UncoveredCell( mine ));
		listeners.forEach( v -> {
			for (int i = 0; i < numberOfMines; i++) {
				int l = positionMines[i] / numberOfColumns;
				int c = positionMines[i] % numberOfColumns;
				mine.notifyEvent(v, l, c);
			}
		});
		gameOver = true;
		listeners.forEach( v -> v.playerLose() );
	}

}
