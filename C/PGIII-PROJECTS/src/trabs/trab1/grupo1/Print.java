package trabs.trab1.grupo1;
import trabs.trab1.grupo1.Time;

public class Print {
    public static void main(String[] args) {
        Time t1 = new Time( 10, 30 );
        Time t2 = new Time( 10, 30 );
        Time t3 = t1; // Alterar

        System.out.println( t1.toString() );
        System.out.println( t1 == t3 );
        System.out.print( t1.equals( t3 ) );
        Time.toTime("04:35:27");
    }
}

/*
Ao executar inicialmento o programa, ambos os outputs retornam "false".
Em 'System.out.println( t1 == t3 );' estamos a verificar seo espaço onde
se encontra a variavel t1 é igual à da variavel t3, o que não e verdade
pois 't3 = t2;' faz com que t3 aponte para o mesmo espaço de memória que t2.

Em 'System.out.print( t1.equals( t3 ) );' este output retorna false pois
a função 'equals' ainda não foi implementada na classe Time, pelo que está
a ser usada a função 'equals' do java que compara as referencias dos objetos.
*/