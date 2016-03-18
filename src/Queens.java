import com.juliasoft.beedeedee.bdd.Assignment;
import com.juliasoft.beedeedee.bdd.BDD;
import com.juliasoft.beedeedee.bdd.UnsatException;
import com.juliasoft.beedeedee.factories.Factory;

public class Queens {

	public static int i, j, k, l;

	public final static int N = 10;
	public final static BDD[][] board = new BDD[N][N];

	public static BDD firstRule;
	public static BDD secondRule;
	public static BDD thirdRule;
	public static BDD fourthRule;

	public static BDD temp;
	public static BDD line;
	public static BDD orChain;
	public static BDD andChain;

	public static BDD result;

	public static void main(String[] args) {

		//long startTime = System.currentTimeMillis();

		Factory factory = Factory.mkResizingAndGarbageCollected(N*N, 10000);

		result = factory.makeOne();
		orChain = factory.makeZero();

		// Initialize the board: each cell contains a variable
		for (i = 0; i < N; ++i) {
			for (j = 0; j < N; ++j) {

				board[i][j] = factory.makeVar(i * N + j);

				temp = orChain.copy();
				orChain.free();

				orChain = board[i][j].or(temp);
				temp.free();
			}

			temp = result.copy();
			result = temp.and(orChain);

			orChain = factory.makeZero();
			temp.free();
		}


		andChain = factory.makeOne();

		for (i = 0; i < N; ++i) {
			for (j = 0; j < N; ++j) {

				BDD tempAnd;

				for (l = 0; l < N && l != j; ++l) {
					/**
					 * First rule
					 */

					// Calcolo il not
					temp = board[i][l].not();

					// Calcolo l'and parziale
					tempAnd = andChain.and(temp);

					andChain.free();
					andChain = tempAnd.copy();

					tempAnd.free();
					temp.free();


				} // Fine and chain

				firstRule = board[i][j].imp(andChain);
				andChain.free();

				andChain = factory.makeOne();

				// Second rule
				for (k = 0; k < N && k != i; ++k) {

					// Calcolo il not
					temp = board[k][j].not();

					// Calcolo l'and parziale
					tempAnd = andChain.and(temp);

					andChain = tempAnd.copy();

					tempAnd.free();
					temp.free();
				} // Fine and chain

				secondRule = board[i][j].imp(andChain);
				andChain.free();
				andChain = factory.makeOne();

				// Third rule
				for (k = 0; k < N && k != i; ++k) {
					if ((j + k -i < N) && (j + k - i >= 0)) {

						temp = board[k][j + k - i].not();

						// Calcolo l'and parziale
						tempAnd = andChain.and(temp);

						andChain = tempAnd.copy();
						tempAnd.free();
						temp.free();
					}
				}

				thirdRule = board[i][j].imp(andChain);
				andChain.free();
				andChain = factory.makeOne();

				// Forth rule
				for (k = 0; k < N && k != i; ++k) {
					if ((j + i -k < N) && (j + i -k >= 0)) {

						temp = board[k][j + i - k].not();

						// Calcolo l'and parziale
						tempAnd = andChain.and(temp);
						andChain.free();

						andChain = tempAnd.copy();
						tempAnd.free();
						temp.free();
					}
				}

				fourthRule = board[i][j].imp(andChain);
				andChain.free();
				andChain = factory.makeOne();


				temp = firstRule.andWith(secondRule.andWith(thirdRule.andWith(fourthRule)));

				BDD temp2 = result.copy();
				result.free();
				result = temp.and(temp2);

				temp.free();
				temp2.free();

			} // j closed
		} // i closed		

		try {
			System.out.println(result.allSat().size());

			// Gets a random assignment
			// printBoard(result.anySat(), result);
		} catch (UnsatException e) { System.out.println("Unsat"); }

		result.free();
		factory.done();

		/*long estimatedTime = System.currentTimeMillis() - startTime;

		int seconds = (int) (estimatedTime / 1000) % 60 ;
		int minutes = (int) ((estimatedTime / (1000 * 60)) % 60);

		System.out.println("Time: " + minutes + "m " + seconds + "s");*/
	}

	private static void printBoard(final Assignment asg, final BDD result) {

		if (asg != null) {
			int c = 0;

			for (int i = 0; i < N*N; ++i) {

				if (result.varProfile()[i] > 0) {
					if (asg.holds(i))
						System.out.print(" 0 ");
					else
						System.out.print(" . ");

					c++;
					if (c % N == 0 && i != 0)
						System.out.println("");
				}
			}
		}
	}
}
