package unirio;

import java.util.ArrayList;

/**
 *
 * @author Aline Guedes
 */
public class Main {
        public static void main(String[] args) {
        String localDoHistorico = args[0];
        
        try {
            SituacaoAlunoUnirio situacaoAlunoUnirio = new SituacaoAlunoUnirio( localDoHistorico );

            System.out.println( "O aluno deve ser jubilado: " + ( situacaoAlunoUnirio.verificaSeAlunoDeveSerJubilado() ? "Sim" : "Não" ) );

            System.out.println( "O aluno deve apresentar plano de integralização: " + ( situacaoAlunoUnirio.verificarSeAlunoDeveApresentarPlanoDeIntegralizacao() ? "Sim" : "Não" ) );

            System.out.println( "O aluno está regularmente matriculado: " + ( situacaoAlunoUnirio.verificaSeAlunoEstaRegularmenteMatriculado() ? "Sim" : "Não" ) );

            System.out.println( "O aluno CR do aluno é maior do que 7,0: " + ( situacaoAlunoUnirio.obtemCrDoAluno() > 7.0 ? "Sim" : "Não" + ", CR: " +  situacaoAlunoUnirio.obtemCrDoAluno()) );

            System.out.print( "O aluno obteve pelo menos nota 5,0 nos semestres definidos no plano de integralização: ");

            ArrayList<String> periodosCr = situacaoAlunoUnirio.verificaSeAlunoObteveNotaMinimaNosSemestresDeIntegralizacao();

            if ( !periodosCr.isEmpty() ) {
                System.out.println("Não");
                for ( String periodoCr: periodosCr ) {
                    System.out.println(periodoCr);
                }
            }
            else {
                System.out.println("Sim");
            }

        }
        catch (Exception e) {
            System.out.println( e.getMessage() );
        }

    }    
}
