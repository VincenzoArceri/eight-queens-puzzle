import com.juliasoft.beedeedee.bdd.Assignment;
import com.juliasoft.beedeedee.bdd.BDD;
import com.juliasoft.beedeedee.bdd.UnsatException;
import com.juliasoft.beedeedee.factories.Factory;

public class Queens {

	public final static int N = 12;
	public static BDD[][] board = new BDD[N][N];

	public static void main(String[] args) {

		long startTime = System.currentTimeMillis();

		Factory factory = Factory.mkResizingAndGarbageCollected(1000, 1000);

		// Initialize the board: each cell contains a variable
		for (int i = 0; i < N; ++i) 
			for (int j = 0; j < N; ++j) 
				board[i][j] = factory.makeVar(i * N + j);

		BDD andChain = factory.makeOne();

		BDD firstRule;
		BDD secondRule;
		BDD thirdRule;
		BDD fourthRule;
		BDD sigletonBDD = factory.makeOne();

		for (int i = 0; i < N; ++i) {
			for (int j = 0; j < N; ++j) {

				// First rule
				for (int l = 0; l < N && l != j; ++l) {

					// Calcolo il not
					BDD temp = board[i][l].not();

					// Calcolo l'and parziale
					BDD tempAnd = andChain.and(temp);

					andChain.free();
					andChain = tempAnd.copy();

					tempAnd.free();
					temp.free();
				} // Fine and chain

				firstRule = board[i][j].imp(andChain);
				andChain.free();

				andChain = factory.makeOne();

				// Second rule
				for (int k = 0; k < N && k != i; ++k) {

					// Calcolo il not
					BDD temp = board[k][j].not();

					// Calcolo l'and parziale
					BDD tempAnd = andChain.and(temp);

					andChain = tempAnd.copy();

					tempAnd.free();
					temp.free();
				} // Fine and chain

				secondRule = board[i][j].imp(andChain);
				andChain.free();
				andChain = factory.makeOne();

				// Third rule
				for (int k = 0; k < N && k != i; ++k) {
					if ((j + k -i < N) && (j + k - i >= 0)) {

						BDD temp = board[k][j + k - i].not();

						// Calcolo l'and parziale
						BDD tempAnd = andChain.and(temp);

						andChain = tempAnd.copy();
						tempAnd.free();
						temp.free();
					}
				}

				thirdRule = board[i][j].imp(andChain);
				andChain.free();
				andChain = factory.makeOne();

				// Forth rule
				for (int k = 0; k < N && k != i; ++k) {
					if ((j + i -k < N) && (j + i -k >= 0)) {

						BDD temp = board[k][j + i - k].not();

						// Calcolo l'and parziale
						BDD tempAnd = andChain.and(temp);
						andChain.free();

						andChain = tempAnd.copy();
						tempAnd.free();
						temp.free();
					}
				}

				fourthRule = board[i][j].imp(andChain);
				andChain.free();
				andChain = factory.makeOne();


				BDD temp = firstRule.and(secondRule.and(thirdRule.and(fourthRule)));

				firstRule.free();
				secondRule.free();
				thirdRule.free();
				fourthRule.free();

				BDD temp2 = sigletonBDD.copy();
				sigletonBDD.free();
				sigletonBDD = temp.and(temp2);

				temp.free();
				temp2.free();

			} // j closed
		} // i closed		

		BDD line = factory.makeZero();

		for (int i = 0; i < N; ++i) {
			for (int j = 0; j < N; ++j) {
				BDD temp = line.copy();
				line.free();

				line = board[i][j].or(temp);
				temp.free();
				board[i][j].free();
			}

			BDD temp = sigletonBDD.copy();
			sigletonBDD.free();
			sigletonBDD = temp.and(line);

			// Clean

			temp.free();
			line.free();
			line = factory.makeZero();
		}

		Assignment a = null;
		try {
			System.out.println(sigletonBDD.allSat().size());
			a = sigletonBDD.anySat();
		} catch (UnsatException e) {

		}

		if (a != null) {
			int c = 0;

			for (int i = 0; i < N*N; ++i) {

				if (sigletonBDD.varProfile()[i] > 0) {
					if (a.holds(i))
						System.out.print(" 0 ");
					else
						System.out.print(" . ");

					c++;
					if (c % N == 0 && i != 0)
						System.out.println("");
				}
			}
		}

		sigletonBDD.free();
		factory.done();

		long estimatedTime = System.currentTimeMillis() - startTime;

		int seconds = (int) (estimatedTime / 1000) % 60 ;
		int minutes = (int) ((estimatedTime / (1000*60)) % 60);
		int hours   = (int) ((estimatedTime / (1000*60*60)) % 24);

		System.out.println("Time: " + minutes + "m " + seconds + "s");
	}
}
