package com.luciano.microservicocadastrarclient.service.serviceimpl

import com.luciano.microservicocadastrarclient.model.AddressClient
import com.luciano.microservicocadastrarclient.model.ClientUser
import com.luciano.microservicocadastrarclient.repository.AddressRepository
import com.luciano.microservicocadastrarclient.repository.ClientRepository
import com.luciano.microservicocadastrarclient.service.CadastreClient
import com.luciano.microservicocadastrarclient.service.ViaCepService
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service


@Service
class CadastreClientImpl(
    private val clientRepository: ClientRepository,
    private val viaCep: ViaCepService,
    private val addressRepository: AddressRepository
) : CadastreClient {

    @Transactional
    override fun cadastreClient(client: ClientUser): ClientUser {
        return clientRepository.save(client).apply {
            if (addressClient == null) {
                addressClient = mutableSetOf()
            }
            val address = getAddress(this)
            addressClient.add(address)
            addressRepository.save(address)
        }
    }

    @Transactional
    override fun getClientById(idClient: Long): ClientUser {
        return clientRepository.findById(idClient).orElseThrow {
            NoSuchElementException("ClientUser not found with id: $idClient ")
        }
    }

    override fun getAllListClients(): List<ClientUser> {
        return clientRepository.findAll()
    }

    @Transactional
    override fun updateClientUser(idClient: Long, client: ClientUser): ClientUser {
        val existingClient = clientRepository.findById(idClient)
            .orElseThrow {
                Exception("ClientUser not found with id: $idClient ")
            }

        return existingClient.copy(
            cep = client.cep,
            phone = client.phone
        ).also { clientRepository.save(it) }
    }

    @Transactional
    override fun deleteClientId(idClient: Long) {
        val existingClient = clientRepository.findById(idClient)
            .orElseThrow {
                NoSuchElementException("ClientUser not found with id: $idClient ")
            }
        clientRepository.delete(existingClient)
    }

    private fun getAddress(client: ClientUser): AddressClient {

        val addressResponse = viaCep.getAddressByCep(client.cep)

        return AddressClient(
            cep = addressResponse.cep,
            road = addressResponse.logradouro ?: "Logradouro não informado",
            city = addressResponse.localidade,
            numberResidence = client.numberResidence ?: "",
            complement = addressResponse.complemento ?: "",
            uf = addressResponse.uf ?: "UF não informada",
            client = client
        )
    }
}
