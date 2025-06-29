import filesys.IFileSystem;
import filesys.Offset;
import filesys.Usuario;

import java.util.Scanner;
import java.io.FileNotFoundException;

import exception.PermissaoException;
import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.OperacaoInvalidaException;
import filesys.FileSystem;

// MENU INTERATIVO PARA O SISTEMA DE ARQUIVOS
// SINTA-SE LIVRE PARA ALTERAR A CLASSE MAIN
public class Main {

    // Constantes úteis para a versão interativa. 
    // Para esse tipo de execução, o tamanho max do buffer de 
    // leitura pode ser menor.
    private static final String ROOT_USER = "root";
    private static final int READ_BUFFER_SIZE = 256;

    // Sistema de arquivos
    private static IFileSystem fileSystem;

    // Scanner para leitura de entrada do usuário
    private static Scanner scanner = new Scanner(System.in);

    // Usuário que está executando o programa
    private static String username;

    // O sistema de arquivos é inteiramente virtual, ou seja, será reiniciado a cada execução do programa.
    // Logo, não é necessário salvar os arquivos em disco. O sistema será uma simulação em memória.
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usuário não fornecido");
            return;
        }
        username = args[1];

        fileSystem = new FileSystem();

        // Carrega a lista de usuários do sistema a partir de arquivo users/users
        try (Scanner userScanner = new Scanner(new java.io.File("users/users"))) {
            while (userScanner.hasNextLine()) {
                String line = userScanner.nextLine().trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    String[] parts = line.split(" ");
                    if (parts.length == 3) {
                        String userListed = parts[0];
                        String dir = parts[1];
                        String dirPermission = parts[2];

                        if (userListed.equals(ROOT_USER)) {
                            continue;
                        }

                        Usuario user = new Usuario(userListed, dirPermission, dir);
                        fileSystem.addUser(user);

                    } else {
                        System.out.println("Formato ruim no arquivo de usuários. Linha: " + line);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo de usuários não encontrado");
            return;
        }

        // Cria os diretórios do arquivo users/users_mkdir
        inicializarDiretoriosDoArquivo(fileSystem, "users/users_mkdir");

        menu();
    }

    private static void inicializarDiretoriosDoArquivo(IFileSystem fileSystem, String caminhoArquivo) {
    try (Scanner scanner = new Scanner(new java.io.File(caminhoArquivo))) {
        while (scanner.hasNextLine()) {
            String linha = scanner.nextLine().trim();
            if (linha.isEmpty() || linha.startsWith("#")) continue;
            String[] partes = linha.split(" ");
            if (partes.length >= 3) {
                String usuario = partes[0];
                String caminho = partes[1];
                String permissao = partes[2];
                try {
                    fileSystem.mkdir(caminho, usuario);
                    fileSystem.chmod(caminho, "root", usuario, permissao);
                } catch (Exception e) {
                    // Ignora erros para diretórios já existentes ou permissões
                }
            }
        }
    } catch (FileNotFoundException e) {
        System.out.println("Arquivo users_mkdir não encontrado.");
    }
}

    // Menu interativo para fins de teste.
    // Os testes junit não são feitos com esse menu,
    // mas diretamente na interface IFileSystem
    public static void menu() {
        while (true) {
            System.out.println("\nComandos disponíveis:");
            System.out.println("1. chmod - Alterar permissões");
            System.out.println("2. mkdir - Criar diretório");
            System.out.println("3. rm - Remover arquivo/diretório");
            System.out.println("4. touch - Criar arquivo");
            System.out.println("5. write - Escrever em arquivo");
            System.out.println("6. read - Ler arquivo");
            System.out.println("7. mv - Mover/renomear arquivo");
            System.out.println("8. ls - Listar diretório");
            System.out.println("9. cp - Copiar arquivo");
            System.out.println("0. exit - Sair");
            System.out.print("\nDigite o comando desejado: ");

            String opcao = scanner.nextLine();
            try {
                switch (opcao) {
                    case "1":
                        chmod();
                        break;
                    case "2":
                        mkdir();
                        break;
                    case "3":
                        rm();
                        break;
                    case "4":
                        touch();
                        break;
                    case "5":
                        write();
                        break;
                    case "6":
                        read();
                        break;
                    case "7":
                        mv();
                        break;
                    case "8":
                        ls();
                        break;
                    case "9":
                        cp();
                        break;
                    case "0":
                        System.out.println("Encerrando...");

                        return;
                    default:
                        System.out.println("Comando inválido!");
                } 
            } catch (CaminhoNaoEncontradoException | CaminhoJaExistenteException | PermissaoException | OperacaoInvalidaException e) {
                System.out.println("Erro: " + e.getMessage());
            }

            System.out.println("Pressione Enter para continuar...");
            scanner.nextLine();
            System.out.print("\033[H\033[2J");
            System.out.flush();
        }
    }

    public static void chmod() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.println("Insira o caminho do arquivo ou diretório:");
        String caminho = scanner.nextLine();
        System.out.println("Insira o usuário para o qual deseja alterar as permissões:");
        String usuarioAlvo = scanner.nextLine();
        System.out.println("Insira a permissão (formato: 3 caracteres\"rwx\"):");
        String permissoes = scanner.nextLine();
        
        fileSystem.chmod(caminho, username, usuarioAlvo, permissoes);
    }

    public static void mkdir() throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException, OperacaoInvalidaException {
        System.out.println("Insira o caminho do diretório a ser criado:");
        String caminho = scanner.nextLine();
        
        fileSystem.mkdir(caminho, username);
    }

    public static void rm() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.println("Insira o caminho do diretório a ser removido:");
        String caminho = scanner.nextLine();
        System.out.println("Remover recursivamente? (true/false):");
        boolean recursivo = Boolean.parseBoolean(scanner.nextLine());
        
        fileSystem.rm(caminho, username, recursivo);
    }

    public static void touch() throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException, OperacaoInvalidaException {
        System.out.println("Insira o caminho do arquivo a ser criado:");
        String caminho = scanner.nextLine();
        
        fileSystem.touch(caminho, username);
    }

    public static void write() throws CaminhoNaoEncontradoException, PermissaoException, OperacaoInvalidaException {
        System.out.println("Insira o caminho do arquivo a ser escrito:");
        String caminho = scanner.nextLine();
        System.out.println("Anexar? (true/false):");
        boolean anexar = Boolean.parseBoolean(scanner.nextLine());
        System.out.println("Insira o conteúdo a ser escrito:");
        String content = scanner.nextLine();
        byte[] buffer = content.getBytes();
        
        fileSystem.write(caminho, username, anexar, buffer);
    }

    public static void read() throws CaminhoNaoEncontradoException, PermissaoException, OperacaoInvalidaException {
        System.out.println("Insira o caminho do arquivo a ser lido:");
        String caminho = scanner.nextLine();
        byte[] buffer = new byte[READ_BUFFER_SIZE];
    
        Offset offset = new Offset(0);
        int offsetAnterior;
        int bytesLidos;
    
        do {
            offsetAnterior = offset.getValue();
            fileSystem.read(caminho, username, buffer, offset);
            bytesLidos = offset.getValue() - offsetAnterior;

            if (bytesLidos > 0) System.out.write(buffer, 0, bytesLidos);
        } while (bytesLidos > 0);

        System.out.flush();
        System.out.println();
    }
    
    public static void mv() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.println("Insira o caminho do arquivo a ser movido:");
        String caminhoAntigo = scanner.nextLine();
        System.out.println("Insira o novo caminho do arquivo:");
        String caminhoNovo = scanner.nextLine();
        
        fileSystem.mv(caminhoAntigo, caminhoNovo, username);
    }

    public static void ls() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.println("Insira o caminho do diretório a ser listado:");
        String caminho = scanner.nextLine();
        System.out.println("Listar recursivamente? (true/false):");
        boolean recursivo = Boolean.parseBoolean(scanner.nextLine());
        
        fileSystem.ls(caminho, username, recursivo);
    }

    public static void cp() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.println("Insira o caminho da origem do arquivo a ser copiado:");
        String caminhoOrigem = scanner.nextLine();
        System.out.println("Insira o caminho do destino do arquivo a ser copiado:");
        String caminhoDestino = scanner.nextLine();
        System.out.println("Copiar recursivamente? (true/false):");
        boolean recursivo = Boolean.parseBoolean(scanner.nextLine());
        
        fileSystem.cp(caminhoOrigem, caminhoDestino, username, recursivo);
    }
}