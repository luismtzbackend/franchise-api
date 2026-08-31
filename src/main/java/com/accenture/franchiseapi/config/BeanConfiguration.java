package com.accenture.franchiseapi.config;

import com.accenture.franchiseapi.application.usecase.ActualizarNombreProducto;
import com.accenture.franchiseapi.application.usecase.ActualizarNombreFranquicia;
import com.accenture.franchiseapi.application.usecase.ActualizarNombreSucursal;
import com.accenture.franchiseapi.application.usecase.ActualizarStockProducto;
import com.accenture.franchiseapi.application.usecase.AgregarProducto;
import com.accenture.franchiseapi.application.usecase.AgregarSucursal;
import com.accenture.franchiseapi.application.usecase.CrearFranquicia;
import com.accenture.franchiseapi.application.usecase.EliminarProducto;
import com.accenture.franchiseapi.application.usecase.ObtenerMayorStockPorFranquicia;
import com.accenture.franchiseapi.domain.port.BranchRepository;
import com.accenture.franchiseapi.domain.port.FranchiseRepository;
import com.accenture.franchiseapi.domain.port.ProductRepository;
import com.accenture.franchiseapi.domain.port.TopStockCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public CrearFranquicia crearFranquicia(FranchiseRepository franchiseRepository) {
        return new CrearFranquicia(franchiseRepository);
    }

    @Bean
    public ActualizarNombreFranquicia actualizarNombreFranquicia(FranchiseRepository franchiseRepository) {
        return new ActualizarNombreFranquicia(franchiseRepository);
    }

    @Bean
    public AgregarSucursal agregarSucursal(BranchRepository branchRepository,
                                          FranchiseRepository franchiseRepository) {
        return new AgregarSucursal(branchRepository, franchiseRepository);
    }

    @Bean
    public ActualizarNombreSucursal actualizarNombreSucursal(BranchRepository branchRepository) {
        return new ActualizarNombreSucursal(branchRepository);
    }

    @Bean
    public AgregarProducto agregarProducto(ProductRepository productRepository,
                                          BranchRepository branchRepository) {
        return new AgregarProducto(productRepository, branchRepository);
    }

    @Bean
    public EliminarProducto eliminarProducto(ProductRepository productRepository) {
        return new EliminarProducto(productRepository);
    }

    @Bean
    public ActualizarStockProducto actualizarStockProducto(ProductRepository productRepository) {
        return new ActualizarStockProducto(productRepository);
    }

    @Bean
    public ActualizarNombreProducto actualizarNombreProducto(ProductRepository productRepository) {
        return new ActualizarNombreProducto(productRepository);
    }

    @Bean
    public ObtenerMayorStockPorFranquicia obtenerMayorStockPorFranquicia(TopStockCache topStockCache,
                                                                         ProductRepository productRepository,
                                                                         BranchRepository branchRepository) {
        return new ObtenerMayorStockPorFranquicia(topStockCache, productRepository, branchRepository);
    }
}
