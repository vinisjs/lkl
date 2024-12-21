import kotlin.collections.*
import kotlin.math.round
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Modelo (Model)
enum class Categoria {
    ELETRONICOS, ROUPAS, ALIMENTOS, ACESSORIOS
}

class Produto(
    val nome: String,
    val preco: Double,
    val categoria: Categoria,
    var estoque: Int,
    var codigo: Int,
    var desconto: Double
) {
    lateinit var descricao: String

    fun informacoes(): String {
        val precoComDesconto = round(preco * (1 - desconto) * 100) / 100
        val descricaoProduto = if (::descricao.isInitialized) descricao else "Descrição não atribuída"
        return """
            Produto: $nome
            Preço: R$ ${"%.2f".format(precoComDesconto)}
            Categoria: $categoria
            Estoque: $estoque
            Código: $codigo
            Descrição: $descricaoProduto
        """.trimIndent()
    }
}

class Venda(
    val produto: Produto,
    val quantidadeVendida: Int
) {
    val data: String = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    val precoTotal: Double = round(quantidadeVendida * produto.preco * (1 - produto.desconto) * 100) / 100

    fun informacoesVenda(): String = """
        Produto Vendido: ${produto.nome}
        Quantidade: $quantidadeVendida
        Data: $data
        Total: R$ ${"%.2f".format(precoTotal)}
    """.trimIndent()
}

class Loja {
    private val produtos = mutableListOf<Produto>()
    private val vendas = mutableListOf<Venda>()

    fun adicionarProduto(produto: Produto) = produtos.add(produto)

    fun registrarVenda(venda: Venda): Boolean {
        return if (venda.produto.estoque >= venda.quantidadeVendida) {
            venda.produto.estoque -= venda.quantidadeVendida
            vendas.add(venda)
            true
        } else false
    }

    fun listarProdutosPorCategoria(categoria: Categoria) = produtos.filter { it.categoria == categoria }

    fun listarProdutosPorPreco(maxPreco: Double) = produtos.filter { it.preco * (1 - it.desconto) <= maxPreco }

    fun listarProdutosComDesconto() = produtos.filter { it.desconto > 0 }

    fun calcularTotalVendas() = vendas.sumOf { it.precoTotal }

    fun obterResumoVendas() = vendas

    fun buscarProdutoPorCodigo(codigo: Int) = produtos.find { it.codigo == codigo }
}

// Visão (View)
object LojaView {
    fun exibirMenu(): Int {
        println("\n=== MENU ===")
        println("1. Adicionar Produto")
        println("2. Listar Produtos por Categoria")
        println("3. Listar Produtos por Preço Máximo")
        println("4. Listar Produtos com Desconto")
        println("5. Registrar Venda")
        println("6. Mostrar Total de Vendas")
        println("7. Mostrar Resumo de Vendas")
        println("8. Sair")
        print("Escolha uma opção: ")
        return readLine()?.toIntOrNull() ?: -1
    }

    fun lerDadosProduto(): Produto {
        val nome = lerString("Nome do produto: ")
        val preco = lerDouble("Preço do produto: ")
        val categoria = Categoria.valueOf(lerString("Categoria (ELETRONICOS, ROUPAS, ALIMENTOS, ACESSORIOS): ").uppercase())
        val estoque = lerInt("Estoque: ")
        val codigo = lerInt("Código: ")
        val desconto = lerDouble("Desconto (valor entre 0.0 e 1.0): ", 0.0..1.0)

        return Produto(nome, preco, categoria, estoque, codigo, desconto).apply {
            descricao = lerString("Descrição do produto: ")
        }
    }

    fun exibirProdutos(produtos: List<Produto>) {
        if (produtos.isEmpty()) println("Nenhum produto encontrado.")
        else produtos.forEach { println(it.informacoes()) }
    }

    fun exibirMensagem(mensagem: String) = println(mensagem)

    fun exibirResumoVendas(vendas: List<Venda>) {
        if (vendas.isEmpty()) println("Nenhuma venda registrada.")
        else vendas.forEach { println(it.informacoesVenda()) }
    }

    fun exibirTotalVendas(total: Double) = println("Total de vendas: R$ ${"%.2f".format(total)}")

    fun lerInt(mensagem: String): Int {
        print(mensagem)
        return readLine()?.toIntOrNull() ?: 0
    }

    fun lerDouble(mensagem: String, range: ClosedRange<Double>? = null): Double {
        var valor: Double
        do {
            print(mensagem)
            valor = readLine()?.toDoubleOrNull() ?: -1.0
        } while (range != null && valor !in range)
        return valor
    }

    fun lerString(mensagem: String): String {
        print(mensagem)
        return readLine() ?: ""
    }

    fun lerCategoria(): Categoria {
        return Categoria.valueOf(lerString("Categoria (ELETRONICOS, ROUPAS, ALIMENTOS, ACESSORIOS): ").uppercase())
    }
}

// Controlador (Controller)
class LojaController(private val loja: Loja, private val view: LojaView) {
    fun iniciar() {
        while (true) {
            when (view.exibirMenu()) {
                1 -> adicionarProduto()
                2 -> listarProdutosPorCategoria()
                3 -> listarProdutosPorPreco()
                4 -> listarProdutosComDesconto()
                5 -> registrarVenda()
                6 -> mostrarTotalVendas()
                7 -> mostrarResumoVendas()
                8 -> {
                    view.exibirMensagem("Saindo...")
                    return
                }
                else -> view.exibirMensagem("Opção inválida. Tente novamente.")
            }
        }
    }

    private fun adicionarProduto() {
        val produto = view.lerDadosProduto()
        loja.adicionarProduto(produto)
        view.exibirMensagem("Produto '${produto.nome}' adicionado com sucesso!")
    }

    private fun listarProdutosPorCategoria() {
        val categoria = view.lerCategoria()
        view.exibirProdutos(loja.listarProdutosPorCategoria(categoria))
    }

    private fun listarProdutosPorPreco() {
        val maxPreco = view.lerDouble("Preço máximo: ")
        view.exibirProdutos(loja.listarProdutosPorPreco(maxPreco))
    }

    private fun listarProdutosComDesconto() {
        view.exibirProdutos(loja.listarProdutosComDesconto())
    }

    private fun registrarVenda() {
        val codigo = view.lerInt("Código do produto: ")
        val produto = loja.buscarProdutoPorCodigo(codigo)

        if (produto != null) {
            val quantidade = view.lerInt("Quantidade a vender: ")
            val venda = Venda(produto, quantidade)
            if (loja.registrarVenda(venda)) view.exibirMensagem("Venda registrada com sucesso!")
            else view.exibirMensagem("Estoque insuficiente para a venda.")
        } else view.exibirMensagem("Produto não encontrado.")
    }

    private fun mostrarTotalVendas() {
        view.exibirTotalVendas(loja.calcularTotalVendas())
    }

    private fun mostrarResumoVendas() {
        view.exibirResumoVendas(loja.obterResumoVendas())
    }
}

fun main() {
    val loja = Loja()
    val view = LojaView
    val controller = LojaController(loja, view)

    controller.iniciar()
}
