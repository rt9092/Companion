package in.wadersgroup.companion;

/**
 * Created by romil_wadersgroup on 28/2/16.
 */
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Romil
 *
 */
public class Utils {
    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;
                os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }
}
