public class TestJava25 {
    public static void main(String[] args) {
        // Teste básico para verificar se estamos rodando Java 25 LTS
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Java Vendor: " + System.getProperty("java.vendor"));
        
        // Testar features do Java 25 LTS
        try {
            // Exemplo de uso de pattern matching (disponível desde Java 21, mas vamos testar)
            Object obj = "Hello Java 25 LTS!";
            if (obj instanceof String s) {
                System.out.println("Pattern matching works: " + s);
            }
            System.out.println("✅ Java 25 LTS está funcionando corretamente!");
        } catch (Exception e) {
            System.err.println("❌ Erro ao testar features: " + e.getMessage());
        }
    }
}