package org.dvbviewer.controller.data.version.xml;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class VersionParser extends Converter.Factory {

  private Converter.Factory factory = SimpleXmlConverterFactory.create();

  Type t = new TypeToken<VersionRoot>(){}.getType();

  @Override
  public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
    // here you can actually look at the annotations, type, etc.
    if (!type.equals(t)) {
      return null;
    }
    return new WrappedResponseBodyConverter(factory.responseBodyConverter(type, annotations, retrofit));
  }

  private class WrappedResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private Converter<ResponseBody, T> responseBodyConverter;

    public WrappedResponseBodyConverter(Converter<ResponseBody, T> responseBodyConverter) {
      this.responseBodyConverter = responseBodyConverter;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
      String body = "<root>" + value.string().replaceAll("\\<\\?xml(.+?)\\?\\>", "").trim() + "</root>";
      ResponseBody wrapped = ResponseBody.create(value.contentType(), body);
      return responseBodyConverter.convert(wrapped);
    }

  }

}