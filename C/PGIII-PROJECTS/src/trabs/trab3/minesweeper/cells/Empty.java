package trabs.trab3.minesweeper.cells;

import trabs.trab3.minesweeper.Board;
import trabs.trab3.minesweeper.GameListener;

/**
 * A célula Empty quando é destapada expande
 * a açăo às oito células adjacentes.
 * Quando é informada que tem um mina adjacente coloca
 * uma célula adjacente a minas na sua posiçăo.
 */
public class Empty extends DummyCell {
    @Override
    public int uncover(Board board, int line, int column) {
        int count = 1;

        board.uncoveredCell(line, column, this);

        for (int dl = -1; dl <= 1; dl++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dl != 0 || dc != 0) {
                    int adjL = line + dl, adjC = column + dc;

                    Cell adjacentCell = board.getCell(adjL, adjC);
                    count += adjacentCell.uncover(board, adjL, adjC);
                }
            }
        }

        return count;
    }

    @Override
    public void adjacentMine(Board board, int line, int column) {
        board.coveredCell(line, column, new AdjacentMine());
    }

    @Override
    public void notifyEvent(GameListener gameListener, int line, int column) {
        gameListener.cellUncover(line, column);
    }
}
