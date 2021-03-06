package dama.model.pieces;

import dama.model.board.Move;
import dama.model.board.Move.*;
import dama.model.board.Board;
import dama.model.board.Board.Builder;
import dama.model.board.BoardUtils;
import dama.model.board.Tile;
import dama.model.Alliance;

import com.google.common.collect.ImmutableList;

import java.util.*;

public class AttackDama extends Piece {

	private List<Piece> attackedPieces;

	private static final int[] CANDIDATE_MOVE_COORDINATES = {-9, -7, 7, 9};

	public AttackDama(final int piecePosition,
					  final Alliance pieceAlliance,
					  final List<Piece> attackedPieces) {
		super(PieceType.DAMA, piecePosition, pieceAlliance);
		this.attackedPieces = new ArrayList<>();
		this.attackedPieces.addAll(attackedPieces);
	}

	@Override
	public Collection<Move> calculateLegalMoves(final Board board) {
		final List<Move> legalMoves = new ArrayList<>();

		for(final int candidateCoordinateOffset : CANDIDATE_MOVE_COORDINATES) {
			if(isFirstColumnExclusion(this.pieceAlliance, this.piecePosition, candidateCoordinateOffset) ||
				isLastColumnExclusion(this.pieceAlliance, this.piecePosition, candidateCoordinateOffset)) continue;

			int candidateDestinationCoordinate = this.piecePosition + (this.pieceAlliance.getDirections() * candidateCoordinateOffset);	
			
			final List<Piece> addedPieces = new ArrayList<>();
			addedPieces.addAll(this.attackedPieces);

			if(BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {
				final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate);
				if(candidateDestinationTile.isTileOccupied()){
					final Piece candidateAttackPiece = candidateDestinationTile.getPiece();
					final Alliance pieceAlliance = candidateAttackPiece.getPieceAlliance();
					candidateDestinationCoordinate += (this.pieceAlliance.getDirections() * candidateCoordinateOffset);
					
					if(BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {
						final Tile candidateAttackDestinationTile = board.getTile(candidateDestinationCoordinate);
						if(candidateAttackDestinationTile.isTileOccupied()) continue;
						if(this.pieceAlliance != pieceAlliance && 
						   !BoardUtils.isTileOnTheEdge(candidateAttackPiece.getPiecePosition()) &&
						   !candidateAttackDestinationTile.isTileOccupied()) {
						   	addedPieces.add(candidateAttackPiece);
							legalMoves.add(new AdditionalAttackMove(board, this, candidateDestinationCoordinate, addedPieces));
						}
					}
				}
			}
		}

		return ImmutableList.copyOf(legalMoves);
	}

	@Override
	public AttackDama movePiece(Move move) {
		return new AttackDama(move.getDestinationCoordinate(), move.getMovedPiece().getPieceAlliance(), move.getAttackedPieces());
	}

	@Override
	public String toString() {
		return "Attack " + Piece.PieceType.DAMA.toString();
	}

	private static boolean isFirstColumnExclusion(final Alliance currentAlliance, final int currentPosition, final int candidateOffset) {
		return (currentAlliance.isBlack()) ? BoardUtils.FIRST_COLUMN.get(currentPosition) && (candidateOffset == -9 || candidateOffset == 7) :
												BoardUtils.FIRST_COLUMN.get(currentPosition) && (candidateOffset == -7 || candidateOffset == 9);
	}

	private static boolean isLastColumnExclusion(final Alliance currentAlliance, final int currentPosition, final int candidateOffset) {
		return (currentAlliance.isBlack()) ? BoardUtils.LAST_COLUMN.get(currentPosition) && (candidateOffset == -7 || candidateOffset == 9) :
												BoardUtils.LAST_COLUMN.get(currentPosition) && (candidateOffset == -9 || candidateOffset == 7);
	}
}