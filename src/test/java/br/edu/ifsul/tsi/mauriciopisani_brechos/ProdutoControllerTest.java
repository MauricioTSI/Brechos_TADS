package br.edu.ifsul.tsi.mauriciopisani_brechos;


import br.edu.ifsul.tsi.mauriciopisani_brechos.api.produtos.Produto;
import br.edu.ifsul.tsi.mauriciopisani_brechos.api.produtos.ProdutoDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MauriciopisaniBrechosApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProdutoControllerTest extends BaseAPITest {

    //Métodos utilitários
    private ResponseEntity<ProdutoDTO> getProduto(String url) {
        return get(url, ProdutoDTO.class);
    }

    private ResponseEntity<List<ProdutoDTO>> getProdutos(String url) {
        HttpHeaders headers = getHeaders();

        return rest.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<List<ProdutoDTO>>() {
            });
    }

    @Test
    public void selectAll() {
        List<ProdutoDTO> produtos = getProdutos("/api/v1/produtos").getBody();
        assertNotNull(produtos);
        assertEquals(3, produtos.size());

        produtos = getProdutos("/api/v1/produtos?page=0&size=5").getBody();
        assertNotNull(produtos);
        assertEquals(3, produtos.size());
    }

    @Test
    public void selectByNome() {

        assertEquals(1, getProdutos("/api/v1/produtos/nome/nesquik").getBody().size());
        assertEquals(1, getProdutos("/api/v1/produtos/nome/nescau").getBody().size());
        assertEquals(1, getProdutos("/api/v1/produtos/nome/nesquik uva").getBody().size());

        assertEquals(HttpStatus.NO_CONTENT, getProdutos("/api/v1/produtos/nome/xxx").getStatusCode());
    }

    @Test
    public void selectById() {

        assertNotNull(getProduto("/api/v1/produtos/1"));
        assertNotNull(getProduto("/api/v1/produtos/2"));
        assertNotNull(getProduto("/api/v1/produtos/3"));

        assertEquals(HttpStatus.NOT_FOUND, getProduto("/api/v1/produtos/1000").getStatusCode());
    }

    @Test
    public void testInsert() {

        Produto produto = new Produto();
        produto.setDescricao("Leite longa vida LG 1L");
        produto.setEstoque(100L);
        produto.setNome("Leite");
        produto.setSituacao(true);
        produto.setValor(new BigDecimal("6.90"));

        // Insert
        ResponseEntity response = post("/api/v1/produtos", produto, null);
        System.out.println(response);

        // Verifica se criou
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        // Buscar o objeto
        String location = response.getHeaders().get("location").get(0);
        ProdutoDTO p = getProduto(location).getBody();

        assertNotNull(p);
        assertEquals("Leite", p.getNome());
        assertEquals(Long.valueOf(100), p.getEstoque());

        // Deletar o objeto
        delete(location, null);

        // Verificar se deletou
        assertEquals(HttpStatus.NOT_FOUND, getProduto(location).getStatusCode());
    }

    @Test
    public void testUpdate() {
        //primeiro insere o objeto
        Produto produto = new Produto();
        produto.setDescricao("Leite longa vida LG 1L");
        produto.setEstoque(100L);
        produto.setNome("Leite");
        produto.setSituacao(true);
        produto.setValor(new BigDecimal(6.90));

        // Insert
        ResponseEntity response = post("/api/v1/produtos", produto, null);
        System.out.println(response);

        // Verifica se criou
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        // Buscar o objeto
        String location = response.getHeaders().get("location").get(0);
        ProdutoDTO p = getProduto(location).getBody();

        assertNotNull(p);
        assertEquals("Leite", p.getNome());
        assertEquals(Long.valueOf(100), p.getEstoque());

        //depois altera seu valor
        Produto pa = Produto.create(p);
        pa.setEstoque(500L);

        // Update
        response = put("/api/v1/produtos/" + p.getId(), pa, null);
        System.out.println(response);
        assertEquals(Long.valueOf(500), pa.getEstoque());

        // Deletar o objeto
        delete(location, null);

        // Verificar se deletou
        assertEquals(HttpStatus.NOT_FOUND, getProduto(location).getStatusCode());

    }

    @Test
    public void testDelete() {
        this.testInsert();
    }

    @Test
    public void testGetNotFound() {
        ResponseEntity response = getProduto("/api/v1/produtos/1100");
        assertEquals(response.getStatusCode(), HttpStatus.NOT_FOUND);
    }
}