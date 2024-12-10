import com.example.springboot.app.asset.AssetService
import com.example.springboot.app.service.PrintScriptService
import com.example.springboot.app.utils.*
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.ResponseEntity
import org.springframework.mock.web.MockMultipartFile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockitoExtension::class)
class PrintScriptServiceTest {

    @Mock
    lateinit var assetService: AssetService

    @InjectMocks
    lateinit var printScriptService: PrintScriptService

    private val mockFile = MockMultipartFile("file", "content".toByteArray())

    @Test
    fun `should validate snippet successfully`() {
        `when`(assetService.getSnippet("snippetId")).thenReturn(ResponseEntity.ok(mockFile))

        val result = printScriptService.validateSnippet("1.1", "snippetId")
        assertEquals(null, result.error)
    }

    @Test
    fun `should return error when validation fails`() {
        `when`(assetService.getSnippet("snippetId")).thenThrow(RuntimeException("Asset not found"))

        val result = printScriptService.validateSnippet("1.1", "snippetId")
        assertEquals("Asset not found", result.error)
    }


    @Test
    fun `should return error when execution fails`() {
        `when`(assetService.getSnippet("snippetId")).thenThrow(RuntimeException("Execution error"))

        val result = printScriptService.executeSnippet("1.1", "snippetId")
        assertEquals("Execution error", result.error)
    }

    @Test
    fun `should execute snippet test successfully`() {
        val runTestDTO = RunTestDTO("input", null, emptyList(), emptyList(), null)
        `when`(assetService.getSnippet("snippetId")).thenReturn(ResponseEntity.ok(mockFile))

        val result = printScriptService.executeSnippetTest("1.1", "snippetId", runTestDTO)
        assertNotNull(result.error)
    }

    @Test
    fun `should return error when snippet test execution fails`() {
        `when`(assetService.getSnippet("snippetId")).thenThrow(RuntimeException("Execution error"))

        val runTestDTO = RunTestDTO("input", null, emptyList(), emptyList(), null)
        val result = printScriptService.executeSnippetTest("1.1", "snippetId", runTestDTO)
        assertEquals("Execution error", result.error)
    }

    @Test
    fun `should fetch multipart file successfully`() {
        `when`(assetService.getSnippet("snippetId")).thenReturn(ResponseEntity.ok(mockFile))

        val file = printScriptService.fetchMultipartFile("snippetId")
        assertNotNull(file)
    }

    @Test
    fun `should generate file from multipart`() {
        val file = printScriptService.genFile(mockFile, "ps")
        assertNotNull(file)
    }

    @Test
    fun `should generate multipart file from file`() {
        val tempFile = printScriptService.genFile(mockFile, "ps")
        val multipartFile = printScriptService.genMultiPartFile(tempFile)
        assertNotNull(multipartFile)
    }


    @Test
    fun `should return null output and error for unknown interpreter result`() {
        val snippetContent = "unknown result"
        val mockFile = MockMultipartFile("file", snippetContent.toByteArray())
        val formatRequest = FormatRequest("id", "userID", "ruleId")
        val lintRequest = LintRequest("id", "ruleId", "userId")
        val validateRequest = ValidateRequest("1.1", "sId")
        val validateResponse = ValidateResponse(null, null)


        `when`(assetService.getSnippet("snippetId")).thenReturn(ResponseEntity.ok(mockFile))

        val result = printScriptService.executeSnippet("1.1", "snippetId")
        assertNull(result.output)
    }

    @Test
    fun `should handle exception during snippet execution`() {
        `when`(assetService.getSnippet("snippetId")).thenThrow(RuntimeException("Snippet not found"))

        val result = printScriptService.executeSnippet("1.1", "snippetId")
        assertNull(result.output)
        assertEquals("Snippet not found", result.error)
    }

    @Test
    fun `should generate valid File from MultipartFile`() {
        val file = printScriptService.genFile(mockFile, "ps")
        assertNotNull(file)
        assertEquals("file", file.name.substring(0, 4)) // Verifying the file prefix
    }

    @Test
    fun `should generate valid MultipartFile from File`() {
        val tempFile = printScriptService.genFile(mockFile, "ps")
        val multipartFile = printScriptService.genMultiPartFile(tempFile)
        assertNotNull(multipartFile)
        assertEquals(tempFile.name, multipartFile.name)
    }

}
