package unirio;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

/**
 *
 * @author Aline Guedes
 */

public class SituacaoAlunoUnirio {
    // array de strings para guardar todas as linhas do historico do aluno
    private ArrayList<String> linhasDoHistorico = new ArrayList<>(); 
    // array de strings para guardar todas as disciplinas cursadas pelo aluno
    private ArrayList<String> disciplinas = new ArrayList<>(); 
    //array de strings para guardar a situação de cada disciplina cursada pelo aluno
    private ArrayList<String> disciplinasSituacao = new ArrayList<>(); 
    
    /**
    * construtor da classe 'situacaoAlunoUnirio' que recebe como parametro o local 
    * onde esta armazenado o historico e executa os metodos 
    * 'extrairLinhasDoHistorico' para armazenar todas as linhas do historico na
    * variavel local 'linhasDoHistorico' e o metodo 'obtemDisciplinasComSituacaoDoAluno'
    * para armazenar todas as disciplinas cursadas pelo aluno e sua situacao
    * nas variaveis locais 'disciplinas' e 'disciplinasSituacao'
    *
    * @param  localDoHistorico caminho absoluto onde se encontra o historico
    */
    public SituacaoAlunoUnirio( String localDoHistorico ) throws IOException {
        extrairLinhasDoHistorico( localDoHistorico ); 
        obtemDisciplinasComSituacaoDoAluno();
    }
    
    /**
    * Funçao que extrai as linhas do historico do  
    *
    * @param  localDoHistorico caminho absoluto onde se encontra o historico
    */
    private void extrairLinhasDoHistorico( String localDoHistorico ) throws IOException {
        if ( localDoHistorico.length() == 0 ) {
            throw new RuntimeException( "O local onde se encontra o historico não foi passado" );
        }

        PdfReader leitorDePdf = new PdfReader( localDoHistorico );

        for ( int pagina = 1; pagina <= leitorDePdf.getNumberOfPages(); pagina++ ) {
            String dadosDoPdf = PdfTextExtractor.getTextFromPage( leitorDePdf, pagina );
            String[] linhas = dadosDoPdf.split( "\n" );
            linhasDoHistorico.addAll( Arrays.asList( linhas ) );
        }
    }

    private void obtemDisciplinasComSituacaoDoAluno() {
        for ( String linha: linhasDoHistorico ) {
            if ( linha.contains( "HTD" ) || linha.contains( "TIN" ) || linha.contains( "TME" ) ) {
                String[] palavras = linha.split( " " );
                String disciplina = palavras[0];

                disciplinas.add( disciplina );

                if ( linha.contains( "APV" ) ) {
                    disciplinasSituacao.add( "APV" );
                }
                else if ( linha.contains( "REP" ) ) {
                    disciplinasSituacao.add( "REP" );
                }
                else if ( linha.contains( "REF" ) ) {
                    disciplinasSituacao.add( "REF" );
                }
                else if ( linha.contains( "TRA" ) ) {
                    disciplinasSituacao.add( "TRA" );
                }
                else if ( linha.contains( "ASC" ) ) {
                    disciplinasSituacao.add( "ASC" );
                }
                else if ( linha.contains( "DIS" ) ) {
                    disciplinasSituacao.add( "DIS" );
                }
            }
        }
    }

    public boolean verificaSeAlunoDeveSerJubilado() throws ParseException {
        Double cr = obtemCrDoAluno();
        int periodoDeInscricao = obtemPeriodoDeInscricao();
        int periodoAtual = obtemPeriodoAtual();
        
        //Indicar que o aluno deve ser jubilado se tem CRA menor que 4,0
        if ( cr > 4 ) {
            return false;
        }

        //Se entrou a partir de 2014.1 não pode exceder 12 semestres sob qualquer hipótese.
        if ( periodoDeInscricao >= 20141 ) {
            if ( periodoAtual > 12 ) {
                return true;
            }
        }

        //e quatro ou mais reprovações em uma mesma disciplina
        for ( int i = 0; i < disciplinas.size(); i++ ) {
            int totalReprovacoes = 0;
            for ( int j = i; j < disciplinas.size(); j++ ) {
                if ( disciplinas.get( j ).equals( disciplinas.get( i ) ) ) {
                    if ( disciplinasSituacao.get( j ).equals( "REP" ) || disciplinasSituacao.get( j ).equals( "REF" ) ) {
                        totalReprovacoes++;
                    }
                }
            }
            if ( totalReprovacoes >= 4 ) {
                return true;
            }
        }

        return false;
    }

    public boolean verificarSeAlunoDeveApresentarPlanoDeIntegralizacao() {
        int periodoDeInscricao = obtemPeriodoDeInscricao();
        int periodoAtual = obtemPeriodoAtual();

        //Se entrou até 2013.2, o prazo máximo de integralização é de 7 anos
        if ( periodoDeInscricao <= 20132 ) {
            //Então deve pedir prorrogação no sexto ano
            if( periodoAtual >= 12 ) {
                return true;
            }
        }
        //Se entrou a partir de 2014.1, o prazo máximo de integralização é de 12 semestres
        else {
            //Deve pedir prorrogação no sétimo período
            if ( periodoAtual >= 7 ) {
                return true;
            }
        }

        return false;
    }

    //Se o aluno está cursando ao menos 3 disciplinas (exceto os formandos).
    public boolean verificaSeAlunoEstaRegularmenteMatriculado() {
        int totalDeMateriasCursadas = 0;
        int totalDeMateriasMatriculado = 0;

        for ( String situacao: disciplinasSituacao ) {
            if ( situacao.equals( "APV" ) || situacao.equals( "DIS" ) ) {
                totalDeMateriasCursadas++;
            }

            if ( situacao.equals( "ASC" ) ) {
                totalDeMateriasMatriculado++;
            }
        }

        // exceto os formandos
        if ( totalDeMateriasCursadas > 48 ) {
            if ( totalDeMateriasMatriculado >= 1 ) {
                return true;
            }
        }

        if ( totalDeMateriasMatriculado >= 3 ) {
            return true;
        }

        return false;
    }

    //O aluno deve manter pelo menos nota 5,0 nos semestres definidos no plano de integralização.
    public ArrayList<String> verificaSeAlunoObteveNotaMinimaNosSemestresDeIntegralizacao() throws ParseException {
        int periodoDeInscricao = obtemPeriodoDeInscricao();
        int periodoAtual = obtemPeriodoAtual();
        int periodo = 0;
        ArrayList<String> periodosCr = new ArrayList<>();

        for ( String linha: linhasDoHistorico ) {
            if ( linha.contains( "semestre de" ) ) {
                periodo++;
            }

            if ( linha.contains( "Coeficiente de Rendimento:" ) ) {
                Double cr = obtemCrDaLinha(linha);

                if ( cr < 5.0 ) {
                    if ( periodoDeInscricao <= 20132 && periodo > 12 && periodo < periodoAtual ) {
                        periodosCr.add( "Periodo: " + periodo + ", CR: " + cr );
                    }

                    if ( periodoDeInscricao >= 20141 && periodo > 7 && periodo < periodoAtual ) {
                        periodosCr.add( "Periodo: " + periodo + ", CR: " + cr );
                    }
                }
            }
        }



        return periodosCr;
    }

    //Se o CR do aluno é maior ou menor que 7,0.
    public Double obtemCrDoAluno() throws ParseException {
        Double cr = 0.0;

        for ( String linha: linhasDoHistorico ) {
            if ( linha.contains("Coeficiente de Rendimento Geral" ) ) {
                cr = obtemCrDaLinha( linha );
            }
        }

        return cr;
    }

    private Double obtemCrDaLinha ( String linha ) throws ParseException {
        Double cr = 0.0;

        String[] palavras = linha.split( " " );
        String crString = palavras[ palavras.length - 1 ];
        
        NumberFormat instance = DecimalFormat.getInstance( new Locale( "pt", "BR" ) );
        instance.setMaximumFractionDigits( 4 );

        if ( !crString.contains(",0000" ) ) {
            cr = ( Double ) instance.parse( crString );
        }
        else {
            long crLong = ( Long ) instance.parse( crString );
            cr = ( double ) crLong;
        }

        return cr;
    }

    private int obtemPeriodoDeInscricao() {
        int periodoDeInscricao = 0;

        for ( String linha: linhasDoHistorico ) {
            if ( linha.contains( "Matrícula" ) ) {
                String[] palavras = linha.split( " " );
                String matricula = palavras[1];
                periodoDeInscricao = Integer.parseInt( matricula.substring( 0, 5 ) );
                break;
            }
        }

        if ( periodoDeInscricao == 0 ) {
            throw new RuntimeException( "Não foi possível obter o periodo em que o aluno se matriculou" );
        }

        return periodoDeInscricao;
    }

    private int obtemPeriodoAtual() {
        int periodoAtual = 0;

        for ( String linha: linhasDoHistorico ) {
            if ( linha.contains( "Período Atual" ) ) {
                String[] palavras = linha.split( " " );
                String periodo = palavras[2];
                periodoAtual = Integer.parseInt( periodo.substring( 0, periodo.length() - 1 ) );
                break;
            }
        }

        if ( periodoAtual == 0 ) {
            throw new RuntimeException( "Não foi possível obter o periodo atual do aluno" );
        }

        return periodoAtual;
    }
}
