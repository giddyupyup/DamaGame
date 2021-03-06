package dama.model.player.ai;

import dama.model.Alliance;
import dama.model.board.Board;
import dama.model.board.Move;
import dama.model.board.MoveTransition;
import dama.model.pieces.Piece;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

import java.util.Collection;
import java.util.Comparator;

public class AlphaBetaPruning implements MoveStrategy {

	private final BoardEvaluator boardEvaluator;
	private final int searchDepth;

	public AlphaBetaPruning(final int searchDepth) {
		this.boardEvaluator = StandardBoardEvaluator.get();
		this.searchDepth = searchDepth;
	}

	@Override
	public String toString() {
		return "AlphaBetaPruning";
	}

	@Override
	public Move execute(Board board) {

		final long startTime = System.currentTimeMillis();

		Move bestMove = null;

		int highestSeenValue = Integer.MIN_VALUE;
		int lowestSeenValue = Integer.MAX_VALUE;
		int currentValue;

		System.out.println(board.getCurrentPlayer() + " THINKING with depth = " + this.searchDepth);

		int numMoves = board.getCurrentPlayer().getLegalMoves().size();

		for(final Move move : sortMove(board.getCurrentPlayer().getLegalMoves())) {
			final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
			if(moveTransition.getMoveStatus().isDone()) {
				currentValue = board.getCurrentPlayer().getAlliance().isWhite() ? 
							   this.min(move, moveTransition.getTransitionBoard(), this.searchDepth - 1, highestSeenValue, lowestSeenValue) :
							   this.max(move, moveTransition.getTransitionBoard(), this.searchDepth - 1, highestSeenValue, lowestSeenValue);
				if(board.getCurrentPlayer().getAlliance().isWhite() && currentValue > highestSeenValue) {
				   	highestSeenValue = currentValue;
				   	bestMove = move;
				   	if(moveTransition.getTransitionBoard().getPlayer(Alliance.BLACK).isGameOver() ||
				   	   moveTransition.getTransitionBoard().getPlayer(Alliance.BLACK).isDraw()) break;
				} else if(board.getCurrentPlayer().getAlliance().isBlack() && currentValue < lowestSeenValue) {
					lowestSeenValue = currentValue;
					bestMove = move;
					if(moveTransition.getTransitionBoard().getPlayer(Alliance.WHITE).isGameOver() ||
				   	   moveTransition.getTransitionBoard().getPlayer(Alliance.WHITE).isDraw()) break;
				}
			}
		}

		final long executionTime = System.currentTimeMillis() - startTime;

		return bestMove;
	}

	public int min(final Move moved, final Board board, final int depth, final int alpha, final int beta) {
		if(depth == 0 || isEndGameScenario(board) || moved.isAttack()) {
			return this.boardEvaluator.evaluate(moved, board, depth);
		}

		int lowestSeenValue = beta;

		for(final Move move : sortMove(board.getCurrentPlayer().getLegalMoves())) {
			final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
			if(moveTransition.getMoveStatus().isDone()) {
				lowestSeenValue = Math.min(lowestSeenValue, this.max(move, moveTransition.getTransitionBoard(), depth - 1, alpha, lowestSeenValue));
				if(lowestSeenValue <= alpha) return alpha;
			}
		}
		return lowestSeenValue;
	}

	public int max(final Move moved, final Board board, final int depth, final int alpha, final int beta) {
		if(depth == 0 || isEndGameScenario(board) || moved.isAttack()) {
			return this.boardEvaluator.evaluate(moved, board, depth);
		}

		int highestSeenValue = alpha;

		for(final Move move : sortMove(board.getCurrentPlayer().getLegalMoves())) {
			final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
			if(moveTransition.getMoveStatus().isDone()) {
				highestSeenValue = Math.max(highestSeenValue, this.min(move, moveTransition.getTransitionBoard(), depth - 1, highestSeenValue, beta));
				if(highestSeenValue >= beta) return beta;
			}
		}
		return highestSeenValue;
	}

	private static boolean isEndGameScenario(final Board board) {
		return board.getCurrentPlayer().isGameOver();
	}

	private static boolean isDrawGameScenario(final Board board) {
		return board.getCurrentPlayer().isDraw();
	}

	private static boolean hasAttackMoves(final Board board) {
		return board.getCurrentPlayer().hasAttackMoves();
	}

	private static Collection<Move> sortMove(final Collection<Move> moves) {
		return Ordering.from(new Comparator<Move>() {
			@Override
			public int compare(final Move move1, final Move move2) {
				return ComparisonChain.start()
						.compareTrueFirst(move1.isAttack(), move2.isAttack())
						.compare(moveValue(move1), moveValue(move2))
						.result();
			}
		}).immutableSortedCopy(moves);
	}

	private static int moveValue(final Move move) {
		if(move.isAttack()) {
			int attackPieces = 0;
			for(final Piece piece : move.getAttackedPieces()) {
				attackPieces += piece.getPieceValue();
			}
			return (attackPieces + move.getMovedPiece().getPieceValue()) * 100;
		}
		return move.getMovedPiece().getPieceValue();
	}
}